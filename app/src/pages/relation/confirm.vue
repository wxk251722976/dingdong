<template>
  <div class="container">
    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <text class="loading-text">加载中...</text>
    </div>
    
    <!-- 邀请信息卡片 -->
    <div class="invite-card" v-if="!loading && inviteInfo">
      <div class="avatar-wrapper">
        <image 
          class="avatar" 
          :src="inviteInfo.avatar || '/static/default-avatar.png'" 
          mode="aspectFill"
        />
      </div>
      <div class="invite-content">
        <div class="invite-title">{{ inviteInfo.nickname }} 邀请你</div>
        <div class="invite-relation">成为Ta的「{{ inviteInfo.relationName }}」</div>
      </div>
    </div>
    
    <!-- 未登录提示 -->
    <div class="login-prompt" v-if="!loading && !isLoggedIn">
      <text class="prompt-text">请先登录后再确认绑定</text>
      <button class="login-btn" @click="goLogin">微信一键登录</button>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-buttons" v-if="!loading && isLoggedIn && inviteInfo">
      <button class="btn btn-accept" @click="handleAccept" :disabled="submitting">
        {{ submitting ? '处理中...' : '接受绑定' }}
      </button>
      <button class="btn btn-reject" @click="handleReject" :disabled="submitting">
        拒绝
      </button>
    </div>
    
    <!-- 错误状态 -->
    <div class="error-state" v-if="!loading && errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
      <button class="btn btn-back" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '@/utils/request';

const loading = ref(true);
const submitting = ref(false);
const isLoggedIn = ref(false);
const inviteInfo = ref(null);
const errorMsg = ref('');

// 邀请参数
const inviteUserId = ref(null);
const relationName = ref('好友');
const activityId = ref(null);

onMounted(async () => {
  // 检查登录状态
  const token = uni.getStorageSync('token');
  isLoggedIn.value = !!token;
  
  // 解析邀请参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage?.options || {};
  
  if (options.inviteUserId) {
    inviteUserId.value = options.inviteUserId;
    relationName.value = decodeURIComponent(options.relationName || '好友');
    activityId.value = options.activityId || null;
    
    // 获取邀请者信息
    await loadInviteInfo();
  } else {
    errorMsg.value = '无效的邀请链接';
    loading.value = false;
  }
});

const loadInviteInfo = async () => {
  try {
    // 尝试获取邀请者用户信息
    const result = await request({
      url: `/user/info/${inviteUserId.value}`,
      method: 'GET'
    });
    
    inviteInfo.value = {
      userId: inviteUserId.value,
      nickname: result?.nickname || `用户${inviteUserId.value}`,
      avatar: result?.avatar || '',
      relationName: relationName.value
    };
  } catch (e) {
    // 即使获取用户信息失败，也显示基本信息
    inviteInfo.value = {
      userId: inviteUserId.value,
      nickname: `用户${inviteUserId.value}`,
      avatar: '',
      relationName: relationName.value
    };
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  // 跳转到登录页，登录后返回此页面
  const currentPage = getCurrentPages()[getCurrentPages().length - 1];
  let fullPath = `/${currentPage.route}?inviteUserId=${inviteUserId.value}&relationName=${encodeURIComponent(relationName.value)}`;
  if (activityId.value) {
    fullPath += `&activityId=${activityId.value}`;
  }
  uni.setStorageSync('redirectAfterLogin', fullPath);
  uni.navigateTo({ url: '/pages/login/index' });
};

const handleAccept = async () => {
  if (submitting.value) return;
  submitting.value = true;
  
  try {
    const user = uni.getStorageSync('user');
    
    // 创建绑定关系（直接以已接受状态创建）
    await request({
      url: '/relation/bind',
      method: 'POST',
      data: {
        supervisedId: user.id,
        supervisorId: inviteUserId.value,
        relationName: relationName.value
      }
    });
    
    // 如果有activityId，更新动态消息状态
    if (activityId.value) {
      try {
        await request({
          url: '/wechat/updateDynamicMsg',
          method: 'POST',
          data: {
            activityId: activityId.value,
            status: 'accepted'
          }
        });
      } catch (e) {
        // 更新动态消息失败不影响主流程
        console.error('更新动态消息失败:', e);
      }
    }
    
    uni.showToast({
      title: '绑定成功',
      icon: 'success'
    });
    
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' });
    }, 1500);
  } catch (e) {
    console.error('接受邀请失败', e);
    uni.showToast({
      title: e.message || '操作失败',
      icon: 'none'
    });
  } finally {
    submitting.value = false;
  }
};

const handleReject = async () => {
  if (submitting.value) return;
  
  uni.showModal({
    title: '确认拒绝',
    content: '确定要拒绝这个绑定邀请吗？',
    success: async (res) => {
      if (res.confirm) {
        submitting.value = true;
        
        try {
          // 如果有activityId，更新动态消息状态为已拒绝
          if (activityId.value) {
            try {
              await request({
                url: '/wechat/updateDynamicMsg',
                method: 'POST',
                data: {
                  activityId: activityId.value,
                  status: 'rejected'
                }
              });
            } catch (e) {
              console.error('更新动态消息失败:', e);
            }
          }
          
          uni.showToast({
            title: '已拒绝邀请',
            icon: 'none'
          });
          
          setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' });
          }, 1500);
        } finally {
          submitting.value = false;
        }
      }
    }
  });
};

const goHome = () => {
  uni.switchTab({ url: '/pages/home/index' });
};
</script>

<style scoped>
.container {
  padding: 60rpx 40rpx;
  min-height: 100vh;
  background: linear-gradient(180deg, #f0fff8 0%, #ffffff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400rpx;
}
.loading-text {
  color: #999;
  font-size: 28rpx;
}

.invite-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  width: 100%;
  box-shadow: 0 8rpx 32rpx rgba(104, 255, 180, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.avatar-wrapper {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 6rpx solid #68FFB4;
  margin-bottom: 30rpx;
}
.avatar {
  width: 100%;
  height: 100%;
}

.invite-content {
  text-align: center;
}
.invite-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}
.invite-relation {
  font-size: 28rpx;
  color: #666;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}
.prompt-text {
  font-size: 28rpx;
  color: #999;
}
.login-btn {
  width: 400rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 44rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  width: 100%;
}
.btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
}
.btn-accept {
  background: linear-gradient(135deg, #68FFB4 0%, #4de3a0 100%);
  color: #333;
}
.btn-reject {
  background: #f5f5f5;
  color: #999;
}
.btn-back {
  background: #68FFB4;
  color: #333;
  width: 300rpx;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40rpx;
  margin-top: 100rpx;
}
.error-text {
  font-size: 28rpx;
  color: #ff6b6b;
}
</style>
