<template>
  <div class="container">
    <!-- 页面标题 -->
    <div class="header">
      <text class="title">我的关系</text>
    </div>

    <!-- 关系列表 -->
    <div class="list-content">
      <div 
        class="user-item" 
        v-for="(item, index) in displayRelations" 
        :key="index"
        @click="goToHistory(item)"
      >
        <image 
          class="avatar" 
          :src="item.otherAvatar || '/static/logo.png'" 
          mode="aspectFill" 
          @error="handleImgError(index)"
        />
        <div class="info">
          <div class="name">{{ item.otherNickname || '用户' }}</div>
          <div class="relation-name">{{ item.relationName || '伙伴关系' }}</div>
        </div>
        
        <!-- 状态标签 -->
        <div class="status-tag" v-if="item.status === 0">
          <text class="pending">待确认</text>
          <button 
            size="mini" 
            type="primary" 
            class="accept-btn"
            @click.stop="acceptInvite(item.id)"
            v-if="item.role === 'PARTNER'"
          >接受</button>
        </div>
        <div class="status-tag" v-else-if="item.status === 3">
          <text class="unbinding">解绑中</text>
          <text class="countdown" v-if="item.unbindExpireTime">
            {{ formatCountdown(item.unbindExpireTime) }}
          </text>
        </div>
        <div class="status-tag" v-else-if="item.status === 4">
          <text class="unbound">已解绑</text>
        </div>
        <div class="arrow" v-else>
          <text class="arrow-icon">›</text>
        </div>
      </div>
      
      <div class="empty-tip" v-if="displayRelations.length === 0">
        <image class="empty-icon" src="/static/empty.png" mode="aspectFit" />
        <text>暂无关系，点击右下角添加</text>
      </div>
    </div>

    <!-- 添加按钮 -->
    <div class="fab" @click="goAdd">
      <text class="fab-text">+</text>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  data() {
    return {
      relations: []
    };
  },
  computed: {
    // 只显示有效的关系（已接受、待确认、解绑中）
    // 已解绑的关系可以选择是否显示
    displayRelations() {
      return this.relations.filter(r => 
        r.status === 0 || // 待确认
        r.status === 1 || // 已接受
        r.status === 3    // 解绑中
      );
    }
  },
  onShow() {
    this.fetchRelations();
  },
  methods: {
    async fetchRelations() {
      try {
        console.log('--- 开始获取关系列表 ---');
        const relations = await request({
          url: '/relation/listWithUserInfo'
        });
        
        console.log('=== 关系列表原始数据 ===', JSON.stringify(relations, null, 2));
        console.log('=== 关系列表是否数组 ===', Array.isArray(relations));
        console.log('=== 关系列表长度 ===', relations?.length);
        
        // 数据清洗：处理类型转换和无效数据
        this.relations = (relations || []).map(r => {
          // 1. 确保 status 是数字
          const status = Number(r.status);
          
          // 2. 检查头像路径是否合法 (过滤掉类似 "1" 这种异常数据)
          let validAvatar = r.otherAvatar;
          if (validAvatar && !validAvatar.startsWith('http') && !validAvatar.startsWith('/static')) {
            validAvatar = null;
          }
          
          return {
            ...r,
            status: isNaN(status) ? r.status : status,
            otherAvatar: validAvatar
          };
        });
        
        console.log('=== displayRelations ===', JSON.stringify(this.displayRelations, null, 2));
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
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    
    goAdd() {
      uni.navigateTo({ url: '/pages/relation/add' });
    },
    
    goToHistory(item) {
      // 跳转到关系详情/历史页面
      uni.navigateTo({ 
        url: `/pages/relation/detail?id=${item.id}&name=${encodeURIComponent(item.otherNickname || '用户')}&relationName=${encodeURIComponent(item.relationName || '')}`
      });
    },
    
    formatCountdown(unbindExpireTime) {
      if (!unbindExpireTime) return '';
      
      const expireDate = new Date(unbindExpireTime);
      const now = new Date();
      const diff = expireDate - now;
      
      if (diff <= 0) return '即将解绑';
      
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      
      return `${hours}时${minutes}分后生效`;
    },
    
    handleImgError(index) {
      // 图片加载失败时使用默认头像，避免死循环和错误日志
      if (this.relations[index]) {
        // 使用 $set 确保视图更新
        this.$set(this.relations[index], 'otherAvatar', '/static/logo.png');
      }
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F5E9 0%, #F8F8F8 30%);
}

.header {
  padding: 60rpx 30rpx 40rpx;
  text-align: center;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}

.list-content {
  padding: 0 30rpx 120rpx;
}

.user-item {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.user-item:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.08);
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #68FFB4 0%, #4CAF50 100%);
  margin-right: 24rpx;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.relation-name {
  font-size: 24rpx;
  color: #68FFB4;
  margin-top: 8rpx;
  background-color: rgba(104, 255, 180, 0.15);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  display: inline-block;
}

.status-tag {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.status-tag .pending {
  font-size: 22rpx;
  color: #FF9800;
  background-color: rgba(255, 152, 0, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.status-tag .unbinding {
  font-size: 22rpx;
  color: #F44336;
  background-color: rgba(244, 67, 54, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.status-tag .countdown {
  font-size: 20rpx;
  color: #999;
}

.status-tag .unbound {
  font-size: 22rpx;
  color: #999;
  background-color: rgba(153, 153, 153, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.accept-btn {
  font-size: 22rpx !important;
  padding: 0 20rpx !important;
  height: 48rpx !important;
  line-height: 48rpx !important;
  background-color: #68FFB4 !important;
  color: #fff !important;
  border: none !important;
  border-radius: 24rpx !important;
}

.arrow {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 36rpx;
  color: #ccc;
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.empty-icon {
  width: 200rpx;
  height: 200rpx;
  opacity: 0.5;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 80rpx;
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #68FFB4 0%, #4CAF50 100%);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 60rpx;
  box-shadow: 0 10rpx 40rpx rgba(104, 255, 180, 0.5);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 10rpx 40rpx rgba(104, 255, 180, 0.5);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 15rpx 50rpx rgba(104, 255, 180, 0.6);
  }
}

.fab-text {
  font-size: 60rpx;
  color: #fff;
  line-height: 1;
  font-weight: 300;
}
</style>
