<template>
  <div class="container">
    <!-- 订阅授权提示 -->
    <div class="notify-tip" v-if="!hasRequestedSubscribe" @click="requestSubscribe">
      🔔 开启消息通知，第一时间接收打卡动态
    </div>

    <div class="list-container">
      <div class="user-card" v-for="(item, index) in supervisedUsers" :key="index" @click="viewDetail(item)">
        <image class="avatar" :src="formatAvatar(item.avatar) || '/static/logo.png'" mode="aspectFill" @error="handleImgError(index)"></image>
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
import request, { BASE_URL } from '@/utils/request';
import { TaskStatus } from '@/utils/constants';
import { requestAllSubscribe } from '@/utils/subscribe';

export default {
  data() {
    return {
      supervisedUsers: [],
      hasRequestedSubscribe: false  // 标记是否已请求过订阅
    };
  },
  onShow() {
    this.fetchSupervisedUsers();
    
    // 检查全局授权状态
    if (uni.getStorageSync('has_authorized_all')) {
        this.hasRequestedSubscribe = true;
    }
  },
  methods: {
    /**
     * 请求订阅消息授权
     * 监督者需要接收：打卡完成通知、漏打卡通知
     */
    async requestSubscribe() {
      try {
        const result = await requestAllSubscribe();
        console.log('监督者订阅授权结果:', result);
        this.hasRequestedSubscribe = true;
        // 记录全局授权标志
        uni.setStorageSync('has_authorized_all', true);
      } catch (e) {
        console.error('请求订阅授权失败:', e);
      }
    },
    async fetchSupervisedUsers() {
      try {
        const user = uni.getStorageSync('user');
        if (!user || !user.id) return;
        
        // Call new endpoint to get users with status in one go
        const users = await request({
          url: '/checkIn/supervisor/status'
        });
        console.log('监督列表数据:', users);
        
        this.supervisedUsers = users || [];
      } catch (e) {
        console.error('获取监督列表失败', e);
      }
    },
    viewDetail(item) {
      // 跳转到该用户的任务列表页面
      const nickname = encodeURIComponent(item.nickname || '用户');
      // 使用处理后的头像URL
      const avatarUrl = this.formatAvatar(item.avatar);
      const avatar = encodeURIComponent(avatarUrl || '');
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
    },
    formatAvatar(url) {
        if (!url) return '';
        // 检查常见的绝对路径协议
        if (url.match(/^(http|wxfile|data|blob):/)) {
            return url;
        }
        
        // 拼接 BASE_URL
        // 移除 url 开头的 / 和 BASE_URL 结尾的 / (如果有) 以免重复，这里假设 BASE_URL 规范
        if (url.startsWith('/')) {
            return BASE_URL + url;
        }
        return BASE_URL + '/' + url;
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

.notify-tip {
  font-size: 26rpx;
  color: #07c160;
  background-color: rgba(7, 193, 96, 0.1);
  padding: 20rpx 32rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.notify-tip:active {
  opacity: 0.8;
  transform: scale(0.99);
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
