<template>
  <div class="container">
    <div class="header-info">
      <image class="avatar" :src="userInfo.avatar || '/static/logo.png'" mode="aspectFill"></image>
      <div class="user-name">{{ userInfo.nickname || '用户' }}</div>
    </div>
    
    <div class="task-list">
      <div class="section-title">我给 Ta 设置的任务</div>
      
      <div 
        class="task-item" 
        v-for="(item, index) in tasks" 
        :key="index"
        @click="goToEdit(item)"
      >
        <div class="task-info">
          <div class="task-title">{{ item.title }}</div>
          <div class="task-meta">
            <text class="meta-item">⏰ {{ formatTime(item.remindTime) }}</text>
            <text class="meta-item">{{ repeatTypeText(item.repeatType) }}</text>
          </div>
        </div>
        <div class="task-status">
          <text v-if="item.status === 1" class="status-badge green">已完成</text>
          <text v-else-if="item.status === 2" class="status-badge orange">补打卡</text>
          <text v-else-if="item.status === 3" class="status-badge red">已错过</text>
          <text v-else class="status-badge gray">待完成</text>
        </div>
        <text class="arrow">></text>
      </div>
      
      <div v-if="tasks.length === 0" class="empty-hint">
        暂无任务，点击下方按钮添加
      </div>
    </div>
    
    <button class="add-btn" @click="goToAdd">+ 添加新任务</button>
  </div>
</template>

<script>
import request from '@/utils/request';
import { RepeatType } from '@/utils/constants';
import { formatTimestamp } from '@/utils/dateUtils';

export default {
  data() {
    return {
      userId: null,
      userInfo: {},
      tasks: []
    };
  },
  onLoad(options) {
    this.userId = options.userId;
    if (options.nickname) {
      this.userInfo.nickname = decodeURIComponent(options.nickname);
    }
    if (options.avatar) {
      this.userInfo.avatar = decodeURIComponent(options.avatar);
    }
  },
  onShow() {
    this.fetchTasks();
  },
  methods: {
    async fetchTasks() {
      if (!this.userId) return;
      
      const supervisorId = uni.getStorageSync('user')?.id;
      if (!supervisorId) return;
      
      try {
        const tasks = await request({
          url: '/task/list',
          data: { 
            userId: this.userId
          }
        });
        this.tasks = tasks || [];
      } catch (e) {
        console.error('获取任务列表失败', e);
      }
    },
    formatTime(remindTime) {
      if (!remindTime) return '全天';
      // remindTime now is a timestamp (milliseconds)
      return formatTimestamp(remindTime, 'time');
    },
    repeatTypeText(code) {
      const type = Object.values(RepeatType).find(t => t.code === code);
      return type ? type.name : '未知';
    },
    goToEdit(item) {
      // 跳转到编辑页面
      uni.navigateTo({
        url: `/pages/task/add?mode=edit&taskId=${item.taskId}&userId=${this.userId}`
      });
    },
    goToAdd() {
      // 跳转到新增页面，预选当前用户
      uni.navigateTo({
        url: `/pages/task/add?preSelectUserId=${this.userId}`
      });
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 30rpx;
  padding-bottom: 150rpx;
}

.header-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #eee;
  margin-bottom: 20rpx;
}

.user-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.task-list {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.section-title {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
  border-left: 6rpx solid #68FFB4;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  flex: 1;
}

.task-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.task-meta {
  display: flex;
  gap: 20rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #999;
}

.task-status {
  margin-right: 20rpx;
}

.status-badge {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
}

.green { color: #07c160; background-color: rgba(7, 193, 96, 0.1); }
.orange { color: #FF9800; background-color: rgba(255, 152, 0, 0.1); }
.red { color: #fa5151; background-color: rgba(250, 81, 81, 0.1); }
.gray { color: #999; background-color: #f5f5f5; }

.arrow {
  color: #ccc;
  font-size: 28rpx;
}

.empty-hint {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.add-btn {
  position: fixed;
  bottom: 40rpx;
  left: 30rpx;
  right: 30rpx;
  background-color: #68FFB4;
  color: #333;
  border-radius: 50rpx;
  font-weight: bold;
  font-size: 32rpx;
}
</style>
