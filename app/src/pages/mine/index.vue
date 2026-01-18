<template>
  <div class="container">
    <div class="user-info-card" @click="openEditPopup">
      <div class="avatar-area">
        <image class="avatar" :src="userInfo.avatar || '/static/logo.png'" mode="aspectFill"></image>
      </div>
      <div class="info-text">
        <div class="nickname-row">
          <div class="nickname">{{ userInfo.id ? (userInfo.nickname || '未设置昵称') : '点击登录' }}</div>
          <div class="level-badge" v-if="userInfo.levelName">{{ userInfo.levelName }}</div>
        </div>
        <div class="role-tag">点击修改资料 <view class="arrow-inline"></view></div>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="navTo('/pages/relation/index')">
        <div class="menu-left">
            <text class="icon">👥</text>
            <text class="menu-text">关系管理</text>
        </div>
        <view class="arrow"></view>
      </div>
      
      <div class="menu-item" @click="navTo('/pages/supervisor/index')">
        <div class="menu-left">
            <text class="icon">👀</text>
            <text class="menu-text">任务管理</text>
        </div>
        <view class="arrow"></view>
      </div>

      <div class="menu-item">
        <div class="menu-left">
            <text class="icon">🔔</text>
            <text class="menu-text">通知设置</text>
        </div>
        <view class="arrow"></view>
      </div>

      <div class="menu-item" @click="navTo('/pages/stats/index')">
        <div class="menu-left">
            <text class="icon">📊</text>
            <text class="menu-text">数据统计</text>
        </div>
        <view class="arrow"></view>
      </div>

      <div class="menu-item" @click="navTo('/pages/feedback/index')">
        <div class="menu-left">
            <text class="icon">❓</text>
            <text class="menu-text">帮助与反馈</text>
        </div>
        <view class="arrow"></view>
      </div>
    </div>

    <!-- Edit Profile Popup -->
    <div class="popup-mask" v-if="showEditPopup" @click="closeEditPopup">
      <div class="popup-content" @click.stop>
        <div class="popup-title">修改资料</div>
        
        <div class="form-item">
          <div class="form-label">头像</div>
          <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
            <image class="edit-avatar" :src="editForm.avatar || '/static/logo.png'" mode="aspectFill"></image>
          </button>
        </div>
        
        <div class="form-item">
          <div class="form-label">昵称</div>
           <input type="nickname" class="nickname-input" v-model="editForm.nickname" placeholder="请输入昵称" />
        </div>
        
        <div class="popup-btns">
           <button class="cancel-btn" @click="closeEditPopup">取消</button>
           <button class="confirm-btn" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { uploadAvatar } from '@/utils/upload';

export default {
  data() {
    return {
      userInfo: {},
      showEditPopup: false,
      editForm: {
        nickname: '',
        avatar: ''
      }
    };
  },
  onShow() {
    this.refreshUserInfo();
  },
  methods: {
    async refreshUserInfo() {
        // 先从缓存读取
        let user = uni.getStorageSync('user') || {};
        const token = uni.getStorageSync('token');
        // 如果有token，尝试从服务器获取最新信息
        if (token) {
            try {
                const latestUser = await request({
                    url: '/user/info'
                });
                if (latestUser) {
                    user = latestUser;
                    uni.setStorageSync('user', user);
                }
            } catch (e) {
                console.error('获取用户信息失败', e);
            }
        }
        
        this.userInfo = user;
        if (user.id) {
            this.fetchLevelInfo();
        }
    },
    async fetchLevelInfo() {
        try {
            const config = await request({
                url: '/user/level/myConfig'
            });
            if (config && config.levelName) {
                this.userInfo = { ...this.userInfo, levelName: config.levelName };
            }
        } catch (e) {
            console.error('获取等级信息失败', e);
        }
    },
    navTo(url) {
      uni.navigateTo({ url });
    },
    openEditPopup() {
      if (!this.userInfo.id) {
        uni.navigateTo({ url: '/pages/login/index' });
        return;
      }
      this.editForm = { ...this.userInfo };
      this.showEditPopup = true;
    },
    closeEditPopup() {
      this.showEditPopup = false;
    },
    async onChooseAvatar(e) {
      const { avatarUrl } = e.detail;
      if (!avatarUrl) return;
      
      try {
        // 上传头像到服务器获取永久URL
        const permanentUrl = await uploadAvatar(avatarUrl);
        this.editForm.avatar = permanentUrl;
        uni.showToast({ title: '头像已上传', icon: 'success' });
      } catch (err) {
        console.error('头像上传失败:', err);
        uni.showToast({ title: '头像上传失败', icon: 'none' });
        // 上传失败时仍使用临时路径（可选择不更新）
        // this.editForm.avatar = avatarUrl;
      }
    },
    saveProfile() {
       if(!this.editForm.nickname) {
           uni.showToast({ title: '请输入昵称', icon: 'none' });
           return;
       }
       uni.showLoading({ title: '保存中' });
       request({
           url: '/user/updateProfile',
           method: 'POST',
           data: {
               nickname: this.editForm.nickname,
               avatar: this.editForm.avatar
           }
       }).then(() => {
           // Update local storage
           const updatedUser = { ...this.userInfo, ...this.editForm };
           uni.setStorageSync('user', updatedUser);
           this.userInfo = updatedUser;
           
           uni.showToast({ title: '保存成功' });
           this.closeEditPopup();
       }).catch(err => {
           console.error('保存失败', err);
           if (typeof err === 'string' && err.includes('用户不存在')) {
               uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
               uni.removeStorageSync('user');
               uni.removeStorageSync('token');
               setTimeout(() => {
                   uni.reLaunch({ url: '/pages/login/index' });
               }, 1500);
           }
       }).finally(() => {
           uni.hideLoading();
       });
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

.nickname-row {
  display: flex;
  align-items: center;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-right: 12rpx;
}

.level-badge {
  font-size: 20rpx;
  color: #fff;
  background: linear-gradient(135deg, #68FFB4 0%, #4ECDC4 100%);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.role-tag {
  font-size: 24rpx;
  color: #999999;
  margin-top: 10rpx;
  display: flex;
  align-items: center;
}

.arrow-inline {
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #999;
  border-right: 3rpx solid #999;
  transform: rotate(45deg);
  margin-left: 8rpx;
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
  width: 16rpx;
  height: 16rpx;
  border-top: 4rpx solid #ccc;
  border-right: 4rpx solid #ccc;
  transform: rotate(45deg);
}

/* Popup Styles */
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.popup-content {
  width: 560rpx;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.popup-title {
  text-align: center;
  font-size: 34rpx;
  font-weight: bold;
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.form-label {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 16rpx;
    align-self: flex-start;
}

.avatar-btn {
  padding: 0;
  background: none;
  border: none;
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-btn::after {
    border: none;
}

.edit-avatar {
  width: 140rpx;
  height: 140rpx;
}

.nickname-input {
    width: 100%;
    height: 80rpx;
    background-color: #f5f5f5;
    border-radius: 8rpx;
    padding: 0 20rpx;
    font-size: 30rpx;
    box-sizing: border-box;
    text-align: center;
}

.popup-btns {
    display: flex;
    justify-content: space-between;
    margin-top: 50rpx;
}

.cancel-btn, .confirm-btn {
    width: 45%;
    font-size: 30rpx;
}

.confirm-btn {
    background-color: #68FFB4;
    color: #333;
    font-weight: bold;
}
</style>
