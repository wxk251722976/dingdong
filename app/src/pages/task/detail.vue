<template>
  <div class="container">
    <div class="task-card">
      <div class="task-header">
        <div class="task-title">{{ task.title }}</div>
        <div class="task-status" :class="statusClass">{{ statusText }}</div>
      </div>
      
      <div class="task-desc" v-if="isValidDescription">{{ task.description }}</div>
      
      <!-- 创建者信息 -->
      <div class="creator-info" v-if="task.creatorName">
        <image class="creator-avatar" :src="validCreatorAvatar" mode="aspectFill"></image>
        <div class="creator-detail">
          <div class="creator-name">{{ task.creatorName }} 设置</div>
          <div class="create-time">{{ formatCreateTime(task.createTime) }}</div>
        </div>
      </div>
      
      <div class="info-list">
        <div class="info-item">
          <text class="label">任务类型</text>
          <text class="value type-badge">{{ repeatTypeText }}</text>
        </div>
        
        <div class="info-item">
          <text class="label">提醒时间</text>
          <text class="value">{{ formatTime(task.remindTime) }}</text>
        </div>
        
        <div class="info-item" v-if="task.checkTime">
          <text class="label">打卡时间</text>
          <text class="value green">{{ formatDateTime(task.checkTime) }}</text>
        </div>
        
        <div class="info-item">
          <text class="label">任务状态</text>
          <text class="value" :class="statusClass">{{ statusText }}</text>
        </div>
      </div>
    </div>
    
    <!-- 打卡按钮（仅待完成时显示） -->
    <button 
      v-if="task.status === 0" 
      class="checkin-btn" 
      @click="handleCheckIn"
    >
      立即打卡
    </button>
  </div>
</template>

<script>
import request, { BASE_URL } from '@/utils/request';
import { TaskStatus, RepeatType } from '@/utils/constants';
import { formatTimestamp } from '@/utils/dateUtils';

export default {
  data() {
    return {
      task: {},
      taskId: null
    };
  },
  computed: {
    statusClass() {
      if (this.task.status === TaskStatus.NORMAL.code) return 'green';
      if (this.task.status === TaskStatus.LATE.code) return 'orange';
      if (this.task.status === TaskStatus.MISSED.code) return 'red';
      return 'gray';
    },
    statusText() {
      if (this.task.status === TaskStatus.NORMAL.code) return '已完成';
      if (this.task.status === TaskStatus.LATE.code) return '补打卡';
      if (this.task.status === TaskStatus.MISSED.code) return '已错过';
      return '待完成';
    },
    repeatTypeText() {
      const type = Object.values(RepeatType).find(t => t.code === this.task.repeatType);
      // Use .desc as defined in constants.js
      return type ? type.desc : '未知';
    },
    isValidDescription() {
        if (!this.task.description) return false;
        // 过滤掉临时文件路径或看起来像URL的内容（如果是纯链接通常不直接展示在文本区）
        if (this.task.description.startsWith('http://tmp') || this.task.description.startsWith('wxfile://')) {
            return false;
        }
        return true;
    },
    validCreatorAvatar() {
        const avatar = this.task.creatorAvatar;
        // Basic validation
        if (!avatar || typeof avatar !== 'string') return '/static/logo.png';
        
        // Defensive: Check for obviously wrong values (like timestamps or huge strings)
        if (avatar.length > 300) return '/static/logo.png';
        if (avatar.includes('创建') || avatar.indexOf('202') === 0) return '/static/logo.png'; // Matches "2026-..." pattern
        
        if (avatar.startsWith('http')) return avatar;
        if (avatar.startsWith('/')) return BASE_URL + avatar;
        
        return avatar;
    }
  },
  onLoad(options) {
    if (options.task) {
      try {
        const taskData = JSON.parse(decodeURIComponent(options.task));
        this.task = taskData;
        this.taskId = taskData.taskId;
        // 加载完整任务详情（包含创建者信息）
        this.fetchTaskDetail();
      } catch (e) {
        console.error('解析任务数据失败', e);
      }
    }
  },
  methods: {
    async fetchTaskDetail() {
      if (!this.taskId) return;
      
      try {
        const detail = await request({
          url: '/task/detail',
          data: { taskId: this.taskId }
        });
        
        if (detail) {
          // 合并详情数据
          this.task = { ...this.task, ...detail };
        }
      } catch (e) {
        console.error('获取任务详情失败', e);
      }
    },
    formatTime(remindTime) {
      if (!remindTime) return '全天';
      // remindTime now is a timestamp (milliseconds)
      return formatTimestamp(remindTime, 'time');
    },
    formatDateTime(checkTime) {
      if (!checkTime) return '';
      // checkTime now is a timestamp (milliseconds)
      return formatTimestamp(checkTime, 'datetime');
    },
    formatCreateTime(createTime) {
      if (!createTime) return '';
      // createTime now is a timestamp (milliseconds)
      return formatTimestamp(createTime, 'datetime') + ' 创建';
    },
    handleCheckIn() {
      const userId = uni.getStorageSync('user')?.id;
      if (!userId || !this.task.taskId) return;
      
      uni.showLoading({ title: '打卡中...' });
      
      request({
        url: '/checkIn/do',
        method: 'POST',
        data: { 
          userId,
          taskId: this.task.taskId
        }
      }).then(() => {
        uni.showToast({ title: '打卡成功', icon: 'success' });
        this.task.status = TaskStatus.NORMAL.code;
        this.task.checkTime = Date.now();
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
  padding: 30rpx;
}

.task-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.task-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.task-status {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.task-desc {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
  background-color: #f8f8f8;
  padding: 20rpx;
  border-radius: 12rpx;
  line-height: 1.5;
}

.creator-info {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.creator-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: #eee;
  margin-right: 20rpx;
}

.creator-detail {
  flex: 1;
}

.creator-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.create-time {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 28rpx;
  color: #999;
}

.value {
  font-size: 28rpx;
  color: #333;
}

.type-badge {
  background-color: #E3F5EB;
  color: #159858;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.green { color: #07c160; background-color: rgba(7, 193, 96, 0.1); }
.orange { color: #FF9800; background-color: rgba(255, 152, 0, 0.1); }
.red { color: #fa5151; background-color: rgba(250, 81, 81, 0.1); }
.gray { color: #999; background-color: #f5f5f5; }

.checkin-btn {
  margin-top: 60rpx;
  background-color: #68FFB4;
  color: #333;
  border-radius: 50rpx;
  font-weight: bold;
  font-size: 32rpx;
}
</style>
