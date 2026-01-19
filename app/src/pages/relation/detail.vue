<template>
  <div class="container">
    <!-- 关系信息头部 -->
    <div class="header">
      <image 
        class="avatar" 
        :src="partnerAvatar || '/static/logo.png'" 
        mode="aspectFill"
      />
      <text class="name">{{ partnerName }}</text>
      <text class="relation-tag">{{ relationName || '伙伴关系' }}</text>
    </div>

    <!-- 操作区域 -->
    <div class="actions" v-if="relation && relation.status === 1">
      <button class="action-btn danger" @click="showUnbindModal = true">
        <text class="btn-icon">🔓</text>
        <text>发起解绑</text>
      </button>
    </div>
    
    <!-- 解绑中状态 -->
    <div class="unbinding-info" v-if="relation && relation.status === 3">
      <div class="unbinding-header">
        <text class="unbinding-icon">⏳</text>
        <text class="unbinding-title">解绑进行中</text>
      </div>
      <text class="unbinding-desc">
        解绑将于 {{ formatDateTime(relation.unbindExpireTime) }} 生效
      </text>
      <button class="action-btn secondary" @click="withdrawUnbind">
        <text>撤回解绑</text>
      </button>
    </div>

    <!-- 历史记录 -->
    <div class="history-section">
      <div class="section-header">
        <text class="section-title">关系历史</text>
      </div>
      
      <div class="timeline" v-if="historyList.length > 0">
        <div class="timeline-item" v-for="(item, index) in historyList" :key="index">
          <div class="timeline-dot" :class="getActionClass(item.actionType)"></div>
          <div class="timeline-content">
            <text class="timeline-action">{{ getActionText(item.actionType) }}</text>
            <text class="timeline-reason" v-if="item.reason">{{ item.reason }}</text>
            <text class="timeline-time">{{ formatDateTime(item.createTime) }}</text>
          </div>
        </div>
      </div>
      
      <div class="empty-history" v-else>
        <text>暂无历史记录</text>
      </div>
    </div>

    <!-- 解绑弹窗 -->
    <div class="modal-mask" v-if="showUnbindModal" @click="showUnbindModal = false">
      <div class="modal-content" @click.stop>
        <text class="modal-title">发起解绑</text>
        <text class="modal-desc">
          解绑将在24小时后生效，期间可以撤回。
          确定要解除与 {{ partnerName }} 的关系吗？
        </text>
        <textarea 
          class="reason-input" 
          v-model="unbindReason" 
          placeholder="请输入解绑原因（选填）"
          maxlength="100"
        />
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="showUnbindModal = false">取消</button>
          <button class="modal-btn confirm" @click="confirmUnbind">确定解绑</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  data() {
    return {
      relationId: null,
      partnerName: '',
      partnerAvatar: '',
      relationName: '',
      relation: null,
      historyList: [],
      showUnbindModal: false,
      unbindReason: ''
    };
  },
  onLoad(options) {
    this.relationId = options.id;
    this.partnerName = decodeURIComponent(options.name || '用户');
    this.relationName = decodeURIComponent(options.relationName || '');
    
    this.fetchRelationDetail();
    this.fetchHistory();
  },
  methods: {
    async fetchRelationDetail() {
      if (!this.relationId) return;
      
      try {
        const relations = await request({
          url: '/relation/listWithUserInfo'
        });
        
        this.relation = relations.find(r => r.id == this.relationId);
        if (this.relation) {
          this.partnerAvatar = this.relation.otherAvatar;
          this.partnerName = this.relation.otherNickname || this.partnerName;
          this.relationName = this.relation.relationName || this.relationName;
        }
      } catch (e) {
        console.error('获取关系详情失败', e);
      }
    },
    
    async fetchHistory() {
      if (!this.relationId) return;
      
      try {
        const history = await request({
          url: '/relation/history',
          data: { relationId: this.relationId }
        });
        
        this.historyList = history || [];
      } catch (e) {
        console.error('获取历史记录失败', e);
        // 历史接口可能还未实现，不影响主功能
      }
    },
    
    async confirmUnbind() {
      try {
        await request({
          url: '/relation/unbind/initiate',
          method: 'POST',
          data: {
            relationId: this.relationId,
            reason: this.unbindReason
          }
        });
        
        uni.showToast({ title: '解绑已发起', icon: 'success' });
        this.showUnbindModal = false;
        this.unbindReason = '';
        this.fetchRelationDetail();
        this.fetchHistory();
      } catch (e) {
        console.error('发起解绑失败', e);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    
    async withdrawUnbind() {
      try {
        await request({
          url: '/relation/unbind/withdraw',
          method: 'POST',
          data: { relationId: this.relationId }
        });
        
        uni.showToast({ title: '已撤回', icon: 'success' });
        this.fetchRelationDetail();
        this.fetchHistory();
      } catch (e) {
        console.error('撤回解绑失败', e);
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    
    getActionClass(actionType) {
      const classes = {
        1: 'bind',      // 绑定
        2: 'unbind',    // 发起解绑
        3: 'withdraw',  // 撤回解绑
        4: 'unbound'    // 解绑完成
      };
      return classes[actionType] || '';
    },
    
    getActionText(actionType) {
      const texts = {
        1: '建立关系',
        2: '发起解绑',
        3: '撤回解绑',
        4: '解绑完成'
      };
      return texts[actionType] || '未知操作';
    },
    
    formatDateTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}`;
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding-bottom: 40rpx;
}

.header {
  background: linear-gradient(135deg, #68FFB4 0%, #4CAF50 100%);
  padding: 80rpx 30rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
}

.name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-top: 24rpx;
}

.relation-tag {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  margin-top: 16rpx;
}

.actions {
  padding: 30rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  border: none;
}

.action-btn.danger {
  background-color: #fff;
  color: #F44336;
  box-shadow: 0 4rpx 20rpx rgba(244, 67, 54, 0.2);
}

.action-btn.secondary {
  background-color: #fff;
  color: #333;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  margin-top: 20rpx;
}

.btn-icon {
  font-size: 32rpx;
}

.unbinding-info {
  margin: 30rpx;
  padding: 30rpx;
  background-color: #FFF3E0;
  border-radius: 20rpx;
  text-align: center;
}

.unbinding-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.unbinding-icon {
  font-size: 40rpx;
}

.unbinding-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF9800;
}

.unbinding-desc {
  font-size: 26rpx;
  color: #666;
  display: block;
}

.history-section {
  margin: 30rpx;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}

.section-header {
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.timeline {
  padding-left: 40rpx;
  border-left: 4rpx solid #E0E0E0;
  margin-left: 16rpx;
}

.timeline-item {
  position: relative;
  padding-bottom: 40rpx;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -52rpx;
  top: 8rpx;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background-color: #E0E0E0;
}

.timeline-dot.bind {
  background-color: #4CAF50;
}

.timeline-dot.unbind {
  background-color: #FF9800;
}

.timeline-dot.withdraw {
  background-color: #2196F3;
}

.timeline-dot.unbound {
  background-color: #F44336;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.timeline-action {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.timeline-reason {
  font-size: 24rpx;
  color: #666;
}

.timeline-time {
  font-size: 22rpx;
  color: #999;
}

.empty-history {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 600rpx;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  display: block;
  margin-bottom: 20rpx;
}

.modal-desc {
  font-size: 28rpx;
  color: #666;
  text-align: center;
  display: block;
  line-height: 1.6;
}

.reason-input {
  width: 100%;
  height: 160rpx;
  background-color: #F5F5F5;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-top: 30rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.modal-btn.cancel {
  background-color: #F5F5F5;
  color: #666;
}

.modal-btn.confirm {
  background-color: #F44336;
  color: #fff;
}
</style>
