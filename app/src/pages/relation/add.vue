<template>
  <div class="container">
    <div class="section-title">邀请方式</div>
    
    <!-- 分享邀请链接 -->
    <div class="invite-card" @click="shareInvite">
      <div class="invite-icon">🔗</div>
      <div class="invite-info">
        <div class="invite-title">分享邀请链接</div>
        <div class="invite-desc">通过微信分享邀请好友加入</div>
      </div>
      <div class="invite-arrow">></div>
    </div>

    <div class="section-title">你们的关系是？</div>
    <div class="tags-container">
      <div 
        class="tag" 
        v-for="(tag, i) in tags" 
        :key="i"
        :class="{ active: selectedTag === i }"
        @click="selectedTag = i"
      >
        {{ tag }}
      </div>
    </div>

    <div class="tip-text">
      选择关系后，点击上方分享邀请链接，好友打开后登录即可完成绑定
    </div>
    
    <!-- 分享按钮（隐藏，通过button触发） -->
    <button class="share-btn" open-type="share">立即分享给好友</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tags: ['情侣', '挚友', '家人', '自定义'],
      selectedTag: 0
    }
  },
  // 配置分享内容
  onShareAppMessage() {
    const userId = uni.getStorageSync('user')?.id;
    const nickname = uni.getStorageSync('user')?.nickname || '好友';
    return {
      title: `${nickname}邀请你一起使用叮咚`,
      path: `/pages/login/index?inviteUserId=${userId}&relationName=${this.tags[this.selectedTag]}`,
      imageUrl: '/static/share-cover.png'
    };
  },
  methods: {
    shareInvite() {
      uni.showModal({
        title: '分享邀请',
        content: '请点击下方"立即分享给好友"按钮，或点击右上角"..."选择"发送给朋友"',
        confirmText: '我知道了',
        showCancel: false
      });
    }
  }
}
</script>

<style scoped>
.container {
  padding: 40rpx;
  background-color: #F8F8F8;
  min-height: 100vh;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  margin-top: 40rpx;
  color: #333;
}
.section-title:first-child {
  margin-top: 0;
}

.invite-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}
.invite-icon {
  font-size: 48rpx;
  margin-right: 24rpx;
}
.invite-info {
  flex: 1;
}
.invite-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}
.invite-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}
.invite-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.tag {
  padding: 16rpx 40rpx;
  background-color: #fff;
  border-radius: 40rpx;
  color: #666;
  border: 1rpx solid #eee;
}
.tag.active {
  background-color: #68FFB4;
  color: #333;
  border-color: #68FFB4;
  font-weight: bold;
}

.tip-text {
  margin-top: 40rpx;
  font-size: 26rpx;
  color: #999;
  text-align: center;
  line-height: 1.6;
}

.share-btn {
  background-color: #68FFB4;
  color: #333;
  font-weight: bold;
  border-radius: 50rpx;
  margin-top: 60rpx;
}
</style>
