<template>
  <div class="container">
    <div class="list-container">
      <div class="user-card" v-for="(item, index) in supervisedUsers" :key="index" @click="viewDetail(item)">
        <image class="avatar" :src="item.avatar || '/static/logo.png'" mode="aspectFill" @error="handleImgError(index)"></image>
        <div class="info">
          <div class="name-row">
            <text class="name">{{ item.nickname || '用户' }}</text>
            <text class="relation-badge">{{ item.relationName || '关系' }}</text>
          </div>
            <div class="status-row">
            <template v-if="item.todayStatus === 1"> <!-- 1: Normal/Completed -->
               <text class="status-icon green">✔️</text>
               <text class="status-text green">今日任务已完成</text>
            </template>
            <template v-else-if="item.todayStatus === 3"> <!-- 3: Missed -->
               <text class="status-icon red">⚠️</text>
               <text class="status-text red">有任务已错过</text>
            </template>
            <template v-else>
               <text class="status-icon gray">⏳</text>
               <text class="status-text gray">等待叮咚中</text>
            </template>
            </div>
        </div>
      </div>
      <div class="empty-tip" v-if="supervisedUsers.length === 0">
        暂无监督对象，点击右下角添加
      </div>
    </div>
    
    <div class="fab" @click="addTask">
      <text class="fab-icon">+</text>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { TaskStatus } from '@/utils/constants';

export default {
  data() {
    return {
      supervisedUsers: []
    };
  },
  onShow() {
    this.fetchSupervisedUsers();
  },
  methods: {
    async fetchSupervisedUsers() {
      try {
        const user = uni.getStorageSync('user');
        if (!user || !user.id) return;
        
        // Call new endpoint to get users with status in one go
        const users = await request({
          url: '/checkIn/supervisor/status'
        });
        
        this.supervisedUsers = users || [];
      } catch (e) {
        console.error('获取监督列表失败', e);
      }
    },
    viewDetail(item) {
      // 跳转到该用户的任务列表页面
      const nickname = encodeURIComponent(item.nickname || '用户');
      const avatar = encodeURIComponent(item.avatar || '');
      uni.navigateTo({ 
        url: `/pages/task/list?userId=${item.userId}&nickname=${nickname}&avatar=${avatar}` 
      });
    },
    addTask() {
      uni.navigateTo({ url: '/pages/task/add' });
    },
    handleImgError(index) {
        if (this.supervisedUsers[index]) {
            this.supervisedUsers[index].avatar = '';
        }
    }
  }
}
</script>

<style scoped>
.container {
  background-color: #F8F8F8;
  padding: 30rpx;
  box-sizing: border-box;
}
.user-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}
.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #eee;
  margin-right: 30rpx;
}
.info {
  flex: 1;
}
.name-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}
.name {
  font-size: 34rpx;
  font-weight: bold; 
  color: #333;
  margin-right: 20rpx;
}
.relation-badge {
  font-size: 22rpx;
  color: #159858;
  background-color: #E3F5EB;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-left: 12rpx;
}
.status-row {
  display: flex;
  align-items: center;
}
.status-icon {
  font-size: 28rpx;
  margin-right: 10rpx;
}
.status-text {
  font-size: 28rpx;
}
.green { color: #07c160; }
.red { color: #fa5151; }
.gray { color: #999; }
.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background-color: #68FFB4;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 10rpx 30rpx rgba(104, 255, 180, 0.5);
}
.fab-icon {
  font-size: 60rpx;
  color: #fff;
  line-height: 1;
}
</style>
