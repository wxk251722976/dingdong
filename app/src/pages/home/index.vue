<template>
  <div class="container">
    <div class="header">
      <div class="time">{{ time }}</div>
      <div class="sub-text">今天叮咚了吗？</div>
    </div>

    <!-- 订阅授权提示 -->
    <div class="notify-tip" v-if="!hasRequestedSubscribe" @click="requestSubscribe">
      🔔 开启消息通知，不再错过提醒
    </div>

    <!-- Main Circular Button -->
    <div class="main-action">
      <div class="circle-btn" @click="handleCheckIn" :class="{ disabled: !currentTask }">
        <template v-if="currentTask">
           <div class="btn-title">叮咚：{{ currentTask.title }}</div>
           <div class="btn-sub">点击告诉 Ta</div>
        </template>
        <template v-else>
           <div class="btn-title">今日已完成</div>
           <div class="btn-sub">太棒了！</div>
        </template>
      </div>
    </div>

    <!-- Daily Task List -->
    <div class="task-list-section">
      <div class="section-header">今日任务</div>
      <div class="task-list">
        <div 
          class="task-item" 
          v-for="(item, index) in dailyTasks" 
          :key="index"
          :class="{ done: item.status === 1 }"
          @click="goToTaskDetail(item)"
        >
          <div class="task-info">
            <div class="task-title">{{ item.title }}</div>
            <div class="task-time">
                <text>⏰ {{ formatTime(item.remindTime) }}</text>
                <text v-if="item.status > 0 && item.checkTime" style="margin-left: 20rpx; color: #07c160;">
                    👀 打卡: {{ formatCheckInTime(item.checkTime) }}
                </text>
            </div>
          </div>
           <div class="task-status">
            <text v-if="item.status === 1" class="icon-done">✅</text>
            <text v-else-if="item.status === 2" class="icon-late">补</text>
            <text v-else-if="item.status === 3" class="icon-missed">⚠️</text>
            <text v-else class="icon-pending">⭕</text>
          </div>
        </div>
        <div v-if="dailyTasks.length === 0" class="empty-hint">
          暂无任务，去休息一下吧 ~
        </div>
      </div>
    </div>

    <div class="footer-link" @click="goToHistory">
      查看历史记录 <view class="arrow-link"></view>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { TaskStatus } from '@/utils/constants';
import { formatTimestamp } from '@/utils/dateUtils';
import { requestAllSubscribe } from '@/utils/subscribe';

export default {
  data() {
    return {
      time: '00:00',
      timer: null,
      dailyTasks: [],
      hasRequestedSubscribe: false  // 标记是否已请求过订阅
    };
  },
  computed: {
    // 找到第一个待完成的任务
    currentTask() {
      const pending = this.dailyTasks.find(t => t.status === TaskStatus.PENDING.code);
      return pending || null;
    }
  },
  onLoad() {
    // 检查登录状态
    const token = uni.getStorageSync('token');
    if (!token) {
      // 未登录，跳转到登录页
      uni.reLaunch({ url: '/pages/login/index' });
      return;
    }
    // 自动订阅已移除，改为用户点击触发
    if (uni.getStorageSync('has_authorized_all')) {
        this.hasRequestedSubscribe = true;
    }
  },
  onShow() {
    // 检查登录状态
    const token = uni.getStorageSync('token');
    if (!token) {
      return; // 未登录不调用接口
    }
    this.updateTime();
    this.timer = setInterval(this.updateTime, 1000);
    this.fetchDailyTasks();
  },
  onHide() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    /**
     * 请求订阅消息授权
     * 被监督者需要接收：叮咚提醒通知
     */
    async requestSubscribe() {
      try {
        const result = await requestAllSubscribe();
        console.log('订阅授权结果:', result);
        this.hasRequestedSubscribe = true;
        // 记录全局授权标志
        uni.setStorageSync('has_authorized_all', true);
      } catch (e) {
        console.error('请求订阅授权失败:', e);
      }
    },
    updateTime() {
      const now = new Date();
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      this.time = `${hours}:${minutes}`;
    },
    formatTime(remindTime) {
      if (!remindTime) return '全天';
      // remindTime now is a timestamp (milliseconds)
      return formatTimestamp(remindTime, 'time');
    },
    formatCheckInTime(checkTime) {
      if (!checkTime) return '';
      // checkTime now is a timestamp (milliseconds)
      return formatTimestamp(checkTime, 'time');
    },
    fetchDailyTasks() {
      request({
        url: '/task/daily'
      }).then(data => {
        this.dailyTasks = data || [];
      }).catch(err => {
        console.error('Fetch tasks failed', err);
        this.dailyTasks = [];
      });
    },
    handleCheckIn() {
      if (!this.currentTask) return;
      
      const remindTime = this.currentTask.remindTime;
      if (!remindTime) {
          // If no remind time, assume all day allow or standard logic?
          // Assuming it must have remindTime if it is a timed task.
          this.doCheckIn();
          return;
      }
      
      const now = Date.now();
      const remind = typeof remindTime === 'string' ? new Date(remindTime).getTime() : remindTime;
      const diff = now - remind;
      const minutes = diff / (1000 * 60);
      
      // 1. Too early: More than 30 mins before
      if (minutes < -30) {
          uni.showToast({ title: '还未到打卡时间哦', icon: 'none' });
          return;
      }
      
      // 2. Missed: More than 30 mins after
      if (minutes > 30) {
          uni.showToast({ title: '任务已过期，无法打卡', icon: 'none' });
          // Optionally refresh list to see if status updates
          this.fetchDailyTasks();
          return;
      }
      
      // 3. Normal or Late - Proceed
      const isLate = minutes >= 0;
      const typeText = isLate ? '补卡' : '打卡';
      
      uni.showLoading({ title: `${typeText}中...` });
      this.doCheckIn();
    },
    doCheckIn() {
      request({
        url: '/checkIn/do',
        method: 'POST',
        data: { 
            taskId: this.currentTask.taskId
        }
      }).then(() => {
         uni.showToast({ title: '叮咚成功', icon: 'success' });
         this.fetchDailyTasks();
      }).catch(msg => {
         // Error handled in request interceptor
      }).finally(() => {
         uni.hideLoading();
      });
    },
    goToHistory() {
      uni.navigateTo({
        url: '/pages/history/index'
      });
    },
    goToTaskDetail(item) {
      // 跳转到任务详情页（只读）
      const taskData = encodeURIComponent(JSON.stringify(item));
      uni.navigateTo({
        url: `/pages/task/detail?task=${taskData}`
      });
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  background-color: #F8F8F8;
  padding: 80rpx 40rpx 40rpx;
  box-sizing: border-box;
}

.header {
  text-align: center;
  margin-bottom: 60rpx;
}

.time {
  font-size: 100rpx;
  font-weight: 400;
  color: #333333;
  font-family: 'Helvetica Neue', Helvetica, sans-serif;
  line-height: 1;
}

.sub-text {
  font-size: 36rpx;
  color: #666666;
  margin-top: 20rpx;
}

.notify-tip {
  font-size: 26rpx;
  color: #07c160;
  background-color: rgba(7, 193, 96, 0.1);
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.notify-tip:active {
  opacity: 0.8;
  transform: scale(0.98);
}

.main-action {
  margin-bottom: 60rpx;
  height: 440rpx;
  display: flex;
  align-items: center;
}

.circle-btn {
  width: 440rpx;
  height: 440rpx;
  border-radius: 50%;
  background-color: #68FFB4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 16rpx 40rpx rgba(104, 255, 180, 0.4);
  background: linear-gradient(135deg, #68FFB4 0%, #4DE8A0 100%);
  transition: all 0.3s;
}

.circle-btn:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 20rpx rgba(104, 255, 180, 0.3);
}

.circle-btn.disabled {
  background: #E0E0E0;
  box-shadow: none;
}

.btn-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 12rpx;
}

.btn-sub {
  font-size: 28rpx;
  color: #444444;
}

.task-list-section {
  width: 100%;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  margin-bottom: 40rpx;
}

.section-header {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
  border-left: 8rpx solid #68FFB4;
  line-height: 1;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item.done .task-title {
  text-decoration: line-through;
  color: #999;
}

.task-title {
  font-size: 34rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.task-time {
  font-size: 24rpx;
  color: #999;
}

.task-status {
  font-size: 32rpx;
}

.icon-missed {
  color: #fa5151;
}

.icon-late {
  display: inline-block;
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  background-color: #FF9800;
  color: white;
  border-radius: 50%;
  font-size: 24rpx;
}

.empty-hint {
  text-align: center;
  color: #999;
  padding: 40rpx 0;
  font-size: 28rpx;
}

.footer-link {
  font-size: 28rpx;
  color: #999999;
  padding: 20rpx;
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-link {
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #999;
  border-right: 3rpx solid #999;
  transform: rotate(45deg);
  margin-left: 8rpx;
}
</style>
