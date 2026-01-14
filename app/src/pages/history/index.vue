<template>
  <div class="container">
    <div class="calendar-wrapper">
       <picker mode="date" :value="currentDate" @change="bindDateChange">
         <div class="date-picker">
           📅 {{ currentDate }} <text style="font-size: 24rpx; color: #999; margin-left: 10rpx;">(点击切换)</text>
         </div>
       </picker>
    </div>

    <div class="timeline">
       <div class="section-title">{{ currentDate }} 的记录</div>
       
       <div class="timeline-item" v-for="(item, idx) in historyList" :key="idx">
         <div class="time-col">{{ formatTime(item.task.remindTime) }}</div>
         <div class="content-col" :class="{ missed: item.status === 'MISSED' }">
            <div class="event-text">
              <text class="bold">{{ item.task.title }}</text> 
              <text v-if="item.status === 'COMPLETED'"> - 已完成 </text>
              <text v-else-if="item.status === 'MISSED'"> - 未完成 </text>
              <text v-else> - 待进行 </text>
            </div>
         </div>
         <div class="status-col">
           <text v-if="item.status === 'COMPLETED'" class="icon green">✅</text>
           <text v-else-if="item.status === 'MISSED'" class="icon red">⚠️</text>
           <text v-else class="icon gray">⏳</text>
         </div>
       </div>

       <div v-if="historyList.length === 0" class="empty">当天没有任务记录</div>
    </div>
  </div>
</template>

<script>
// Make sure to install uni-calendar or remove if not available. 
// Assuming standard uni-ui template or similar. If not, standard picker.
// Since I don't see uni_modules in file list, I'll fallback to a simple date picker if calendar isn't there, 
// but user asked for "Calendar View". I'll try to use standard picker for safety or just a header date.
// Actually, let's stick to a simple date header for robustness if no uni-ui.
// Re-writing template above to be safer without external components if not installed.

export default {
  data() {
    return {
      currentDate: '',
      historyList: []
    }
  },
  onLoad() {
    const now = new Date();
    this.currentDate = this.formatDate(now);
    this.fetchHistory(this.currentDate);
  },
  methods: {
    formatDate(date) {
        return date.toISOString().split('T')[0];
    },
    formatTime(timeStr) {
        return timeStr ? timeStr.substring(0, 5) : '--:--';
    },
    bindDateChange(e) {
        this.currentDate = e.detail.value;
        this.fetchHistory(this.currentDate);
    },
    fetchHistory(date) {
        const userId = uni.getStorageSync('user')?.id || 1;
        uni.showLoading();
        uni.request({
            url: 'http://localhost:8080/task/daily',
            method: 'GET',
            data: { userId, date },
            success: (res) => {
                uni.hideLoading();
                if (res.data.code === 200) {
                    this.historyList = res.data.data;
                }
            },
            fail: () => {
                uni.hideLoading();
            }
        });
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
.calendar-wrapper {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.date-picker {
    padding: 20rpx 40rpx;
    background-color: #f0f0f0;
    border-radius: 50rpx;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
}
.timeline {
  padding: 40rpx;
}
.section-title {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 30rpx;
    color: #666;
}
.timeline-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}
.time-col {
  width: 100rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.content-col {
  flex: 1;
  background-color: #fff;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-right: 20rpx;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.03);
}
.content-col.missed {
    background-color: #fff0f0;
}
.event-text {
  font-size: 30rpx;
  color: #333;
}
.bold {
  font-weight: bold;
}
.icon {
  font-size: 40rpx;
}
.green { color: #07c160; }
.red { color: #fa5151; }
.gray { color: #ccc; }
.empty {
    text-align: center;
    color: #999;
    margin-top: 100rpx;
}
</style>
