<template>
  <div class="container">
    <div class="list-container">
      <div class="user-card" v-for="(item, index) in supervisedUsers" :key="index" @click="viewDetail(item)">
        <image class="avatar" :src="item.avatar || '/static/logo.png'" mode="aspectFill"></image>
        <div class="info">
          <div class="name-row">
            <text class="name">{{ item.nickname || '用户' }}</text>
            <text class="relation-badge">{{ item.relationName || '关系' }}</text>
          </div>
          <div class="status-row">
            <template v-if="item.todayStatus === 'COMPLETED'">
               <text class="status-icon green">✔️</text>
               <text class="status-text green">今日任务已完成</text>
            </template>
            <template v-else-if="item.todayStatus === 'MISSED'">
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
        const userId = uni.getStorageSync('user')?.id;
        if (!userId) return;
        
        // 获取我监督的人（我是child/监督者）
        const relations = await request({
          url: '/relation/myRelations',
          data: { userId }
        });
        
        // 筛选出我作为监督者的关系（childId === userId）
        const mySupervised = relations.filter(r => r.childId === userId);
        
        // 获取每个被监督者的今日任务状态
        const users = [];
        for (const relation of mySupervised) {
          try {
            // 获取被监督者信息
            const tasks = await request({
              url: '/task/daily',
              data: { userId: relation.elderId }
            });
            
            // 计算今日整体状态
            let todayStatus = 'PENDING';
            if (tasks.length > 0) {
              const allCompleted = tasks.every(t => t.status === TaskStatus.COMPLETED.code);
              const anyMissed = tasks.some(t => t.status === TaskStatus.MISSED.code);
              if (allCompleted) {
                todayStatus = 'COMPLETED';
              } else if (anyMissed) {
                todayStatus = 'MISSED';
              }
            }
            
            users.push({
              id: relation.elderId,
              nickname: relation.elderNickname || `用户${relation.elderId}`,
              avatar: relation.elderAvatar || '',
              relationName: relation.relationName || '关系',
              todayStatus
            });
          } catch (e) {
            console.error('获取用户任务失败', e);
          }
        }
        
        this.supervisedUsers = users;
      } catch (e) {
        console.error('获取监督列表失败', e);
      }
    },
    viewDetail(item) {
      uni.navigateTo({ url: `/pages/history/index?userId=${item.id}` });
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
