<template>
	<div class="child-container">
		<div class="stat-card">
			<div class="stat-title">今日打卡情况</div>
			<div class="stat-content">
				<div class="stat-item">
					<div class="num">{{ todayCount }}</div>
					<div class="label">已打卡</div>
				</div>
				<div class="stat-item">
					<div class="num safe" v-if="todayCount > 0">正常</div>
					<div class="num warn" v-else>未打卡</div>
					<div class="label">状态</div>
				</div>
			</div>
		</div>

		<div class="list-title">打卡记录</div>
		<div class="log-list">
			<div class="log-item" v-for="(item, index) in checkInLogs" :key="index">
				<div class="log-icon">💊</div>
				<div class="log-info">
					<div class="log-time">{{ formatTime(item.checkTime) }}</div>
					<div class="log-date">{{ formatDate(item.checkTime) }}</div>
				</div>
				<div class="log-status normal">
					{{ item.status === 1 ? '正常打卡' : '补打卡' }}
				</div>
			</div>
			<div class="empty-tip" v-if="checkInLogs.length === 0">
				暂无打卡记录
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import request from '@/utils/request';

const checkInLogs = ref([]);
const todayCount = ref(0);

const fetchLogs = async () => {
	try {
		const user = uni.getStorageSync('user');
		if (!user) return;

		// Assuming we fetch logs for the current user's bound elders.
		// For simplicity in this demo, we might need an endpoint to get logs OF ELDERS.
		// If the user is a child, they want to see Elder's logs.
		// Let's assume the backend 'getLogs' returns logs for the Passed UserId.
		// We first need to find WHO is my elder.
		
		const elders = await request({
			url: '/checkIn/elders?childId=' + user.id,
			method: 'GET'
		});
		
		if (elders && elders.length > 0) {
			// Get logs for the first elder
			const elderId = elders[0].id;
			const logs = await request({
				url: '/checkIn/logs?userId=' + elderId,
				method: 'GET'
			});
			checkInLogs.value = logs;
			
			// Calculate today count
			const today = new Date().toDateString();
			todayCount.value = logs.filter(log => new Date(log.checkTime).toDateString() === today).length;
		} else {
			// No elders bound
			uni.showToast({ title: '未绑定老人', icon: 'none' });
		}
	} catch (e) {
		console.error(e);
	}
};

onShow(() => {
	fetchLogs();
});

const formatTime = (isoStr) => {
	const d = new Date(isoStr);
	return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const formatDate = (isoStr) => {
	const d = new Date(isoStr);
	return `${d.getMonth() + 1}月${d.getDate()}日`;
};
</script>

<style scoped>
.child-container {
	padding: 20px;
	background-color: #f5f5f5;
	min-height: 100vh;
	box-sizing: border-box;
}

.stat-card {
	background: #fff;
	border-radius: 16px;
	padding: 20px;
	margin-bottom: 20px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.stat-title {
	font-size: 16px;
	font-weight: bold;
	color: #333;
	margin-bottom: 15px;
	border-left: 4px solid #07c160;
	padding-left: 10px;
}

.stat-content {
	display: flex;
	justify-content: space-around;
	text-align: center;
}

.num {
	font-size: 24px;
	font-weight: bold;
	color: #333;
	margin-bottom: 5px;
}

.num.safe { color: #07c160; }
.num.warn { color: #ff9800; }

.label {
	font-size: 12px;
	color: #999;
}

.list-title {
	font-size: 16px;
	font-weight: bold;
	color: #333;
	margin-bottom: 15px;
	padding-left: 10px;
}



.log-item {
	background: #fff;
	border-radius: 12px;
	padding: 15px;
	margin-bottom: 12px;
	display: flex;
	align-items: center;
}

.log-icon {
	width: 40px;
	height: 40px;
	background: #f0f9eb;
	border-radius: 50%;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 20px;
	margin-right: 15px;
}

.log-info {
	flex: 1;
}

.log-time {
	font-size: 16px;
	font-weight: bold;
	color: #333;
}

.log-date {
	font-size: 12px;
	color: #999;
}

.log-status {
	font-size: 12px;
	padding: 4px 8px;
	border-radius: 4px;
	background: #f0f0f0;
	color: #666;
}

.log-status.normal {
	background: #e8f5e9;
	color: #4caf50;
}

.empty-tip {
	text-align: center;
	padding-top: 50px;
	color: #999;
}
</style>
