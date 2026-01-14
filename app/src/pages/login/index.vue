<template>
	<div class="login-container">
		<div class="header">
			<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
			<div class="title">DingDong</div>
			<div class="subtitle">家庭关爱提醒助手</div>
		</div>
		
		<!-- 邀请提示 -->
		<div class="invite-banner" v-if="inviteInfo">
			<text class="invite-text">{{ inviteInfo.nickname }} 邀请你成为{{ inviteInfo.relationName }}</text>
		</div>

		<div class="auth-section">
			<button class="login-btn" type="primary" @click="handleLogin">微信一键登录</button>
		</div>
	</div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

const inviteInfo = ref(null);
const inviteUserId = ref(null);
const inviteRelationName = ref(null);

onMounted(() => {
	// 解析邀请参数
	const pages = getCurrentPages();
	const currentPage = pages[pages.length - 1];
	const options = currentPage?.options || {};
	
	if (options.inviteUserId) {
		inviteUserId.value = options.inviteUserId;
		inviteRelationName.value = options.relationName || '好友';
		
		// 显示邀请信息
		inviteInfo.value = {
			nickname: `用户${options.inviteUserId}`,
			relationName: inviteRelationName.value
		};
	}
});

const handleLogin = () => {
	uni.login({
		provider: 'weixin',
		success: async (res) => {
			if (res.code) {
				try {
					const user = await request({
						url: '/auth/login',
						method: 'POST',
						data: {
							code: res.code,
							nickname: '微信用户',
							avatar: ''
						}
					});
					uni.setStorageSync('token', user.openid);
					uni.setStorageSync('user', user);
					
					// 如果是被邀请的用户，自动建立关系
					if (inviteUserId.value) {
						try {
							await request({
								url: '/relation/invite',
								method: 'POST',
								data: {
									elderId: user.id,  // 被邀请者成为被监督者
									childId: inviteUserId.value,  // 邀请者成为监督者
									relationName: inviteRelationName.value
								}
							});
							uni.showToast({ title: '已接受邀请', icon: 'success' });
						} catch (e) {
							console.error('建立关系失败', e);
						}
					}
					
					// 跳转到主页
					uni.switchTab({ url: '/pages/elder/index' });
				} catch (e) {
					console.error(e);
				}
			}
		}
	});
};
</script>

<style scoped>
.login-container {
	padding: 40px 20px;
	display: flex;
	flex-direction: column;
	align-items: center;
	min-height: 100vh;
	background-color: #fff;
}

.header {
	margin-top: 60px;
	margin-bottom: 40px;
	text-align: center;
}

.logo {
	width: 100px;
	height: 100px;
	border-radius: 20px;
	margin-bottom: 20px;
	box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.title {
	font-size: 32px;
	font-weight: bold;
	color: #333;
	margin-bottom: 8px;
}

.subtitle {
	font-size: 16px;
	color: #999;
}

.invite-banner {
	background: linear-gradient(135deg, #68FFB4 0%, #4de3a0 100%);
	padding: 16px 24px;
	border-radius: 12px;
	margin-bottom: 40px;
	width: 80%;
	text-align: center;
}
.invite-text {
	color: #333;
	font-size: 16px;
	font-weight: bold;
}

.login-btn {
	width: 80%;
	height: 50px;
	border-radius: 25px;
	font-size: 18px;
	background-color: #07c160;
	color: #fff;
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 0;
	border: none;
}

.auth-section {
	width: 100%;
	display: flex;
	justify-content: center;
}
</style>
