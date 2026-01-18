<template>
	<div class="login-container">
		<div class="header">
			<!-- 头像选择区域 -->
			<button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
				<image class="avatar-img" :src="userAvatar || '/static/logo.png'" mode="aspectFill"></image>
				<div class="avatar-tip">点击选择头像</div>
			</button>
			<div class="title">DingDong</div>
			<div class="subtitle">家庭关爱提醒助手</div>
		</div>
		
		<!-- 昵称输入（微信新版本需要用户授权） -->
		<div class="nickname-section">
			<input 
				type="nickname" 
				class="nickname-input" 
				placeholder="请输入昵称" 
				v-model="userNickname"
				@blur="onNicknameInput"
			/>
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

// 用户头像和昵称
const userAvatar = ref('');
const userNickname = ref('');

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

// 选择头像回调
const onChooseAvatar = (e) => {
	const avatarUrl = e.detail.avatarUrl;
	if (avatarUrl) {
		userAvatar.value = avatarUrl;
		console.log('用户选择头像:', avatarUrl);
	}
};

// 昵称输入回调
const onNicknameInput = (e) => {
	// 微信 type="nickname" 会自动填充昵称
	console.log('用户昵称:', userNickname.value);
};

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
							nickname: userNickname.value || '微信用户',
							avatar: userAvatar.value || ''
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
