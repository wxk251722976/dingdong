<template>
  <div class="container">
    <div class="section-title">你们的关系是？</div>
    <div class="tags-container">
      <div 
        class="tag" 
        v-for="(tag, i) in tags" 
        :key="i"
        :class="{ active: selectedTag === i }"
        @click="selectTag(i)"
      >
        {{ tag }}
      </div>
    </div>

    <!-- 自定义关系输入框 -->
    <div class="custom-input-container" v-if="selectedTag === 3">
      <input 
        class="custom-input"
        type="text"
        :value="customRelation"
        placeholder="请输入自定义关系（最多5个字）"
        maxlength="5"
        @input="onCustomInput"
      />
      <div class="char-count">{{ customRelation.length }}/5</div>
    </div>

    <div class="tip-text">
      选择关系后，点击下方按钮分享给好友，好友可以选择接受或拒绝绑定
    </div>
    
    <!-- 分享按钮 -->
    <button class="share-btn" open-type="share" @click="prepareShare">立即分享给好友</button>
  </div>
</template>

<script>
import request from '@/utils/request';

// 常量定义
// 0: 情侣 (Type 1)
// 1: 挚友 (Type 2)
// 2: 家人 (Type 3)
// 3: 自定义 (Type 0)
const TAGS = ['情侣', '挚友', '家人', '自定义'];
const RELATION_TYPE_MAP = [1, 2, 3, 0];

export default {
  data() {
    return {
      tags: TAGS,
      selectedTag: 0,
      customRelation: '',
      activityId: null,
      isPreparingShare: false
    }
  },
  computed: {
    // 是否为自定义类型
    isCustomType() {
      return this.selectedTag === 3;
    },
    // 获取最终的关系名称
    finalRelationName() {
      if (this.isCustomType) {
        return this.customRelation.trim() || '好友';
      }
      return this.tags[this.selectedTag];
    },
    // 获取关系类型枚举值
    relationType() {
      return RELATION_TYPE_MAP[this.selectedTag];
    }
  },
  onLoad() {
    // 页面加载时预先获取activityId
    this.fetchActivityId();
  },
  onShow() {
    // 每次显示页面时更新分享菜单
    this.updateShareMenu();
  },
  // 配置分享内容
  onShareAppMessage() {
    const userId = uni.getStorageSync('user')?.id;
    const nickname = uni.getStorageSync('user')?.nickname || '好友';
    return {
      title: `${nickname}邀请你一起使用叮咚`,
      path: `/pages/relation/confirm?inviteUserId=${userId}&relationName=${encodeURIComponent(this.finalRelationName)}&relationType=${this.relationType}&activityId=${this.activityId || ''}`,
      imageUrl: '/static/share-cover.png'
    };
  },
  methods: {
    // 选择标签
    selectTag(index) {
      this.selectedTag = index;
      // 切换到非自定义时清空自定义输入
      if (!this.isCustomType) {
        this.customRelation = '';
      }
    },
    
    // 自定义输入处理
    onCustomInput(e) {
      const val = e.detail.value || '';
      // 限制长度（双重保障）
      if (val.length > 5) {
        this.customRelation = val.slice(0, 5);
      } else {
        this.customRelation = val;
      }
    },
    
    // 获取动态消息活动ID
    async fetchActivityId() {
      try {
        const result = await request({
          url: '/wechat/createActivityId',
          method: 'POST'
        });
        this.activityId = result.activityId;
        console.log('获取到activityId:', this.activityId);
        this.updateShareMenu();
      } catch (e) {
        console.error('获取activityId失败:', e);
      }
    },
    
    // 更新分享菜单，声明为动态消息
    updateShareMenu() {
      if (!this.activityId) return;
      
      // #ifdef MP-WEIXIN
      wx.updateShareMenu({
        withShareTicket: true,
        isUpdatableMessage: true,
        activityId: this.activityId,
        templateInfo: {
          parameterList: [
            { name: 'member_count', value: '1' },
            { name: 'room_limit', value: '2' }
          ]
        },
        success: () => console.log('动态消息分享菜单更新成功'),
        fail: (err) => console.error('更新分享菜单失败:', err)
      });
      // #endif
    },
    
    // 准备分享（点击按钮时校验）
    async prepareShare() {
      // 验证自定义关系输入
      if (this.isCustomType && !this.customRelation.trim()) {
        uni.showToast({
          title: '请输入自定义关系',
          icon: 'none'
        });
        return;
      }
      
      // 如果没有activityId，尝试重新获取
      if (!this.activityId && !this.isPreparingShare) {
        this.isPreparingShare = true;
        await this.fetchActivityId();
        this.isPreparingShare = false;
      }
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
  color: #333;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -10rpx; /* 抵消 margin */
}
.tag {
  padding: 16rpx 40rpx;
  background-color: #fff;
  border-radius: 40rpx;
  color: #666;
  border: 1rpx solid #eee;
  margin: 10rpx; /* 使用 margin 替代 gap */
}
.tag.active {
  background-color: #68FFB4;
  color: #333;
  border-color: #68FFB4;
  font-weight: bold;
}

.custom-input-container {
  margin-top: 30rpx;
  position: relative;
}
.custom-input {
  width: 100%;
  height: 88rpx;
  padding: 0 120rpx 0 30rpx;
  background-color: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #eee;
  font-size: 28rpx;
  box-sizing: border-box;
}
.custom-input:focus {
  border-color: #68FFB4;
}
.char-count {
  position: absolute;
  right: 30rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24rpx;
  color: #999;
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
