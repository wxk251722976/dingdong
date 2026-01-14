<template>
	<div class="login-container">
		<div class="header">
			<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
			<div class="title">DingDong</div>
			<div class="subtitle">家庭关爱提醒助手</div>
		</div>

		<div v-if="!hasRole" class="auth-section">
			<button class="login-btn" type="primary" @click="handleLogin">微信一键登录</button>
		</div>

		<div v-else class="role-section">
			<div class="role-title">请选择您的身份</div>
			<div class="role-cards">
				<div class="role-card elder" @click="selectRole('ELDER')">
					<div class="icon">👴</div>
					<div class="text">我是长辈</div>
					<div class="desc">打卡吃药</div>
				</div>
				<div class="role-card child" @click="selectRole('CHILD')">
					<div class="icon">👶</div>
					<div class="text">我是子女</div>
					<div class="desc">查看记录</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref } from 'vue';
import request from '@/utils/request';

const hasRole = ref(false);
const userInfo = ref(null);

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
							nickname: '微信用户', // 简化，实际应展示简易输入框或获取个人信息
							avatar: ''
						}
					});
					userInfo.value = user;
					uni.setStorageSync('token', user.openid); // 简化，使用openid作为token
					uni.setStorageSync('user', user);
					
					if (user.role) {
						redirect(user.role);
					} else {
						hasRole.value = true; // 显示角色选择
					}
				} catch (e) {
					console.error(e);
				}
			}
		}
	});
};

const selectRole = async (role) => {
	try {
		await request({
			url: '/auth/switchRole',
			method: 'POST',
			data: {
				userId: userInfo.value.id,
				role: role
			}
		});
		userInfo.value.role = role;
		uni.setStorageSync('user', userInfo.value);
		redirect(role);
	} catch (e) {
		console.error(e);
	}
};

const redirect = (role) => {
	if (role === 'ELDER') {
		uni.switchTab({ url: '/pages/elder/index' });
	} else if (role === 'CHILD') {
		uni.switchTab({ url: '/pages/child/index' });
	}
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
	margin-bottom: 60px;
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

.role-section {
	width: 100%;
}

.role-title {
	text-align: center;
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 30px;
	color: #333;
}

.role-cards {
	display: flex;
	justify-content: space-around;
	gap: 20px;
}

.role-card {
	flex: 1;
	background: #f8f8f8;
	border-radius: 16px;
	padding: 20px;
	display: flex;
	flex-direction: column;
	align-items: center;
	box-shadow: 0 4px 12px rgba(0,0,0,0.05);
	transition: all 0.3s;
}

.role-card:active {
	opacity: 0.8;
	transform: scale(0.98);
}

.role-card.elder {
	background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.role-card.child {
	background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.icon {
	font-size: 40px;
	margin-bottom: 10px;
}

.text {
	font-size: 18px;
	font-weight: bold;
	color: #333;
	margin-bottom: 4px;
}

.desc {
	font-size: 12px;
	color: #666;
}
</style>
