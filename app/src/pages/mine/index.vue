<template>
  <div class="container">
    <div class="user-info-card">
      <div class="avatar-area">
        <image class="avatar" src="/static/logo.png" mode="aspectFill"></image> <!-- Placeholder -->
      </div>
      <div class="info-text">
        <div class="nickname">小王子</div>
        <div class="role-tag">VIP 用户</div>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="navTo('/pages/relation/index')">
        <div class="menu-left">
            <text class="icon">👥</text>
            <text class="menu-text">关系管理</text>
        </div>
        <text class="arrow">></text>
      </div>
      
      <div class="menu-item" @click="navTo('/pages/supervisor/index')">
        <div class="menu-left">
            <text class="icon">👀</text>
            <text class="menu-text">监督中心 (临时入口)</text>
        </div>
        <text class="arrow">></text>
      </div>

      <div class="menu-item">
        <div class="menu-left">
            <text class="icon">🔔</text>
            <text class="menu-text">通知设置</text>
        </div>
        <text class="arrow">></text>
      </div>

      <div class="menu-item">
        <div class="menu-left">
            <text class="icon">📊</text>
            <text class="menu-text">数据统计</text>
        </div>
        <text class="arrow">></text>
      </div>

      <div class="menu-item">
        <div class="menu-left">
            <text class="icon">❓</text>
            <text class="menu-text">帮助与反馈</text>
        </div>
        <text class="arrow">></text>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    navTo(url) {
      uni.navigateTo({ url });
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 40rpx;
}

.user-info-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: #e0e0e0;
  margin-right: 30rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.role-tag {
  font-size: 24rpx;
  color: #999999;
  margin-top: 10rpx;
}

.menu-list {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 0 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-left {
    display: flex;
    align-items: center;
}

.icon {
    font-size: 36rpx;
    margin-right: 20rpx;
    width: 40rpx;
    text-align: center;
}

.menu-text {
  font-size: 30rpx;
  color: #333333;
}

.arrow {
  color: #cccccc;
  font-size: 30rpx;
}
</style>
