<template>
  <div class="container">
    <div class="tabs">
      <div class="tab-item" :class="{ active: currentTab === 0 }" @click="currentTab = 0">我监督的</div>
      <div class="tab-item" :class="{ active: currentTab === 1 }" @click="currentTab = 1">监督我的</div>
    </div>

    <div class="list-content">
      <div class="user-item" v-for="(item, index) in listData" :key="index">
        <image class="avatar" :src="item.avatar || '/static/logo.png'" mode="aspectFill"></image>
        <div class="info">
          <div class="name">{{ item.name }}</div>
          <div class="desc">{{ item.desc }}</div>
        </div>
        <div class="action" v-if="currentTab===1 && item.status === 0">
           <button size="mini" type="primary">接受</button>
        </div>
      </div>
    </div>

    <div class="fab" @click="goAdd">
      <text class="fab">+</text>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentTab: 0,
      mySupervised: [
         { name: '宝贝', desc: '关联时间: 2025-01-01', avatar: '' }
      ],
      supervisedBy: [
         { name: '老公', desc: '关联时间: 2025-01-02', avatar: '' }
      ]
    };
  },
  computed: {
    listData() {
      return this.currentTab === 0 ? this.mySupervised : this.supervisedBy;
    }
  },
  methods: {
    goAdd() {
      uni.navigateTo({ url: '/pages/relation/add' });
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
</style>
