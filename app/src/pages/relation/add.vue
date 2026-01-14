<template>
  <div class="container">
    <div class="section-title">选择好友</div>
    <div class="user-list">
       <div class="friend-item" :class="{selected: selectedUser===0}" @click="selectedUser=0">
         <image class="avatar" src="/static/logo.png"></image>
         <text class="name">微信好友A</text>
       </div>
       <!-- More mocks -->
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

    <button class="send-btn" @click="sendInvite">发送通知</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedUser: -1,
      tags: ['情侣', '挚友', '家人', '自定义'],
      selectedTag: 0
    }
  },
  methods: {
    sendInvite() {
      uni.showToast({ title: '邀请已发送' });
      setTimeout(() => uni.navigateBack(), 1500);
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
  margin-top: 20rpx;
}
.user-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.friend-item {
  width: 120rpx;
  text-align: center;
  padding: 10rpx;
  border: 4rpx solid transparent;
  border-radius: 12rpx;
}
.friend-item.selected {
  border-color: #68FFB4;
  background-color: #fff;
}
.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: #ddd;
}
.name {
  display: block;
  font-size: 24rpx;
  color: #333;
  margin-top: 10rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
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

.send-btn {
  background-color: #333; /* Dark button for high contrast invites? Or Primary? */
  /* User request says Main Color is Mint Green, Text Dark Grey. */
  /* Let's stick to Mint Green for Primary Actions to refer to theme. */
  background-color: #68FFB4;
  color: #333;
  font-weight: bold;
  border-radius: 50rpx;
  margin-top: 100rpx;
}
</style>
