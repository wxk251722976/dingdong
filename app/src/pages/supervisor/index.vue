<template>
  <div class="container">
    <div class="list-container">
      <div class="user-card" v-for="(item, index) in supervisedUsers" :key="index" @click="viewDetail(item)">
        <image class="avatar" :src="item.avatar || '/static/logo.png'" mode="aspectFill"></image>
        <div class="info">
          <div class="name-row">
            <text class="name">{{ item.name }}</text>
            <text class="relation-badge">{{ item.relation }}</text>
          </div>
          <div class="status-row">
            <template v-if="item.lastTaskStatus === 'DONE'">
               <text class="status-icon green">✔️</text>
               <text class="status-text green">{{ item.lastTaskName }}: 今天已完成</text>
            </template>
            <template v-else-if="item.lastTaskStatus === 'MISSED'">
               <text class="status-icon red">⚠️</text>
               <text class="status-text red">{{ item.lastTaskName }}: 已错过</text>
            </template>
            <template v-else>
               <text class="status-icon gray">⏳</text>
               <text class="status-text gray">{{ item.lastTaskName }}: 等待中</text>
            </template>
          </div>
        </div>
      </div>
    </div>
    
    <div class="fab" @click="addTask">
      <text class="fab-icon">+</text>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      supervisedUsers: [
        { name: '宝贝', relation: '情侣', avatar: '', lastTaskName: '喝水任务', lastTaskStatus: 'DONE' },
        { name: '老妈', relation: '家人', avatar: '', lastTaskName: '运动任务', lastTaskStatus: 'MISSED' },
        { name: '小李', relation: '挚友', avatar: '', lastTaskName: '早睡', lastTaskStatus: 'PENDING' }
      ]
    };
  },
  onShow() {
    // fetch data
  },
  methods: {
    viewDetail(item) {
      // Go to detail or history
    },
    addTask() {
      uni.navigateTo({ url: '/pages/task/add' });
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 30rpx;
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
  color: #68FFB4;
  background-color: rgba(104, 255, 180, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
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

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx; /* Safe area handled by OS mostly, but adding margin */
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
