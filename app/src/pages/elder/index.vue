<template>
  <div class="container">
    <div class="header">
      <div class="time">{{ time }}</div>
      <div class="sub-text">今天叮咚了吗？</div>
    </div>

    <!-- Main Circular Button -->
    <div class="main-action">
      <div class="circle-btn" @click="handleCheckIn" :class="{ disabled: !currentTask }">
        <template v-if="currentTask">
           <div class="btn-title">叮咚：{{ currentTask.title }}</div>
           <div class="btn-sub">点击告诉 Ta</div>
        </template>
        <template v-else>
           <div class="btn-title">今日已完成</div>
           <div class="btn-sub">太棒了！</div>
        </template>
      </div>
    </div>

    <!-- Daily Task List -->
    <div class="task-list-section">
      <div class="section-header">今日任务</div>
      <div class="task-list">
        <div 
          class="task-item" 
          v-for="(item, index) in dailyTasks" 
          :key="index"
          :class="{ done: item.status === 1 }"
        >
          <div class="task-info">
            <div class="task-title">{{ item.title }}</div>
            <div class="task-time">⏰ {{ formatTime(item.remindTime) }}</div>
          </div>
          <div class="task-status">
            <text v-if="item.status === 1" class="icon-done">✅</text>
            <text v-else-if="item.status === 2" class="icon-missed">⚠️</text>
            <text v-else class="icon-pending">⭕</text>
          </div>
        </div>
        <div v-if="dailyTasks.length === 0" class="empty-hint">
          暂无任务，去休息一下吧 ~
        </div>
      </div>
    </div>

    <div class="footer-link" @click="goToHistory">
      查看历史记录 >
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { TaskStatus } from '@/utils/constants';

export default {
  data() {
    return {
      time: '00:00',
      timer: null,
      dailyTasks: [],
    };
  },
  computed: {
    // 找到第一个待完成的任务
    currentTask() {
      const pending = this.dailyTasks.find(t => t.status !== TaskStatus.COMPLETED.code);
      return pending || null;
    }
  },
  onShow() {
    this.updateTime();
    this.timer = setInterval(this.updateTime, 1000);
    this.fetchDailyTasks();
  },
  onHide() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    updateTime() {
      const now = new Date();
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      this.time = `${hours}:${minutes}`;
    },
    formatTime(remindTime) {
      if (!remindTime) return '全天';
      // remindTime格式: "2026-01-14T10:00:00"
      const time = remindTime.split('T')[1];
      return time ? time.substring(0, 5) : '全天';
    },
    fetchDailyTasks() {
      const userId = uni.getStorageSync('user')?.id || 1; 
      
      request({
        url: '/task/daily',
        data: { userId }
      }).then(data => {
        this.dailyTasks = data || [];
      }).catch(err => {
        console.error('Fetch tasks failed', err);
        this.dailyTasks = [];
      });
    },
    handleCheckIn() {
      if (!this.currentTask) return;
      
      const userId = uni.getStorageSync('user')?.id || 1;
      uni.showLoading({ title: '叮咚中...' });
      
      request({
        url: '/checkIn/do',
        method: 'POST',
        data: { 
            userId,
            taskId: this.currentTask.taskId
        }
      }).then(() => {
         uni.showToast({ title: '叮咚成功', icon: 'success' });
         this.fetchDailyTasks();
      }).catch(msg => {
         // Error handled in request interceptor
      }).finally(() => {
         uni.hideLoading();
      });
    },
    goToHistory() {
      uni.navigateTo({
        url: '/pages/history/index'
      });
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 80rpx 40rpx 40rpx;
  box-sizing: border-box;
}

.header {
  text-align: center;
  margin-bottom: 60rpx;
}

.time {
  font-size: 100rpx;
  font-weight: 400;
  color: #333333;
  font-family: 'Helvetica Neue', Helvetica, sans-serif;
  line-height: 1;
}

.sub-text {
  font-size: 36rpx;
  color: #666666;
  margin-top: 20rpx;
}

.main-action {
  margin-bottom: 60rpx;
  height: 440rpx;
  display: flex;
  align-items: center;
}

.circle-btn {
  width: 440rpx;
  height: 440rpx;
  border-radius: 50%;
  background-color: #68FFB4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 16rpx 40rpx rgba(104, 255, 180, 0.4);
  background: linear-gradient(135deg, #68FFB4 0%, #4DE8A0 100%);
  transition: all 0.3s;
}

.circle-btn:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 20rpx rgba(104, 255, 180, 0.3);
}

.circle-btn.disabled {
  background: #E0E0E0;
  box-shadow: none;
}

.btn-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 12rpx;
}

.btn-sub {
  font-size: 28rpx;
  color: #444444;
}

.task-list-section {
  width: 100%;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  margin-bottom: 40rpx;
}

.section-header {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
  border-left: 8rpx solid #68FFB4;
  line-height: 1;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item.done .task-title {
  text-decoration: line-through;
  color: #999;
}

.task-title {
  font-size: 34rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.task-time {
  font-size: 24rpx;
  color: #999;
}

.task-status {
  font-size: 32rpx;
}

.icon-missed {
  color: #fa5151;
}

.empty-hint {
  text-align: center;
  color: #999;
  padding: 40rpx 0;
  font-size: 28rpx;
}

.footer-link {
  font-size: 28rpx;
  color: #999999;
  padding: 20rpx;
  margin-top: auto;
}
</style>
