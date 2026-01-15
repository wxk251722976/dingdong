<template>
  <div class="container">
    <div class="tabs">
      <div class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">我监督的</div>
      <div class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">监督我的</div>
    </div>

    <div class="list-content">
      <div class="user-item" v-for="(item, index) in listData" :key="index">
        <image class="avatar" :src="item.otherAvatar || '/static/logo.png'" mode="aspectFill" @error="handleImgError(index)"></image>
        <div class="info">
          <div class="name">{{ item.otherNickname || '用户' }}</div>
          <div class="desc">{{ item.relationName || '关系' }}</div>
        </div>
        <div class="action" v-if="currentTab === 1 && item.status === 0">
           <button size="mini" type="primary" @click="acceptInvite(item.id)">接受</button>
        </div>
        <div class="status-tag" v-else-if="item.status === 0">
          <text class="pending">待确认</text>
        </div>
      </div>
      <div class="empty-tip" v-if="listData.length === 0">
        暂无数据
      </div>
    </div>

    <!-- 只在"我监督的"标签页显示添加按钮 -->
    <div class="fab" @click="goAdd" v-if="currentTab === 0">
      <text class="fab-text">+</text>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  data() {
    return {
      currentTab: 0,
      relations: []
    };
  },
  computed: {
    listData() {
      if (this.currentTab === 0) {
        // 我监督的（我是supervisor）
        return this.relations.filter(r => r.role === 'SUPERVISOR');
      } else {
        // 监督我的（我是supervised）
        return this.relations.filter(r => r.role === 'SUPERVISED');
      }
    }
  },
  onShow() {
    this.fetchRelations();
  },
  methods: {
    switchTab(index) {
      this.currentTab = index;
    },
    async fetchRelations() {
      try {
        // 使用新的带用户信息的接口
        const relations = await request({
          url: '/relation/listWithUserInfo'
        });
        
        this.relations = relations || [];
      } catch (e) {
        console.error('获取关系列表失败', e);
      }
    },
    async acceptInvite(relationId) {
      try {
        await request({
          url: '/relation/accept',
          method: 'POST',
          data: { id: relationId }
        });
        uni.showToast({ title: '已接受', icon: 'success' });
        this.fetchRelations();
      } catch (e) {
        console.error('接受邀请失败', e);
      }
    },
    goAdd() {
      uni.navigateTo({ url: '/pages/relation/add' });
    },
    handleImgError(index) {
      // 图片加载失败时使用默认头像
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
}
.tabs {
  display: flex;
  background-color: #fff;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
}
.tab-item {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  color: #999;
  position: relative;
  padding-bottom: 20rpx;
}
.tab-item.active {
  color: #333;
  font-weight: bold;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background-color: #68FFB4;
  border-radius: 4rpx;
}

.list-content {
  padding: 30rpx;
}
.user-item {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  background-color: #eee;
  margin-right: 20rpx;
}
.info {
  flex: 1;
}
.name {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}
.desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}
.status-tag .pending {
  font-size: 22rpx;
  color: #FF9800;
  background-color: rgba(255, 152, 0, 0.1);
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
}
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
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #68FFB4;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 60rpx;
  box-shadow: 0 10rpx 30rpx rgba(104, 255, 180, 0.5);
}
.fab-text {
  font-size: 60rpx;
  color: #fff;
  line-height: 1;
}
</style>
