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
			<button class="login-btn" type="primary" @click="handleLogin">微信登录</button>
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
					const result = await request({
						url: '/auth/login',
						method: 'POST',
						data: {
							code: res.code,
							nickname: '微信用户',
							avatar: ''
						}
					});
					// result 包含 { user, token, refreshToken }
					const { user, token, refreshToken } = result;
					uni.setStorageSync('token', token);
					if (refreshToken) {
						uni.setStorageSync('refreshToken', refreshToken);
					}
					uni.setStorageSync('user', user);
					
					// 如果是被邀请的用户，自动建立关系
					if (inviteUserId.value) {
						try {
							await request({
								url: '/relation/invite',
								method: 'POST',
								data: {
									supervisedId: user.id,  // 被邀请者成为被监督者
									supervisorId: inviteUserId.value,  // 邀请者成为监督者
									relationName: inviteRelationName.value
								}
							});
							uni.showToast({ title: '已接受邀请', icon: 'success' });
						} catch (e) {
							console.error('建立关系失败', e);
						}
					}
					
					// 跳转到主页
					uni.switchTab({ url: '/pages/home/index' });
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
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	min-height: 100vh;
	background-color: #fff;
}

.header {
	margin-top: 150rpx;
	margin-bottom: 100rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100%;
}

.logo {
	width: 180rpx;
	height: 180rpx;
	border-radius: 40rpx;
	margin-bottom: 40rpx;
	box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.1);
}

.title {
	font-size: 48rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 16rpx;
}

.subtitle {
	font-size: 32rpx;
	color: #999;
}

.invite-banner {
	background: linear-gradient(135deg, #68FFB4 0%, #4de3a0 100%);
	padding: 16px 24px;
	border-radius: 12px;
	margin-bottom: 60px;
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
	height: 100rpx;
	border-radius: 50rpx;
	font-size: 36rpx;
	background-color: #07c160;
	color: #fff;
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 0;
	border: none;
	box-shadow: 0 10rpx 20rpx rgba(7, 193, 96, 0.3);
}

.auth-section {
	width: 100%;
	display: flex;
	justify-content: center;
}
</style>
