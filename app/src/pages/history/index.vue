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
         <div class="time-col">{{ formatTime(item.remindTime) }}</div>
          <div class="content-col" :class="{ missed: item.status === 2 }">
             <div class="event-text">
               <text class="bold">{{ item.title }}</text> 
               <div v-if="item.status === 1 || item.status === 2" style="font-size: 24rpx; color: #666; margin-top: 8rpx;">
                 打卡时间: {{ formatTime(item.checkTime) }}
               </div>
               <div v-else-if="item.status === 3" style="font-size: 24rpx; color: #fa5151; margin-top: 8rpx;">
                 未打卡
               </div>
             </div>
          </div>
         <div class="status-col">
           <text v-if="item.status === 1" class="icon green">✅</text>
           <text v-else-if="item.status === 2" class="icon red">⚠️</text>
           <text v-else class="icon gray">⏳</text>
         </div>
       </div>

       <div v-if="historyList.length === 0" class="empty">当天没有任务记录</div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { formatTimestamp } from '@/utils/dateUtils';

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
    formatTime(remindTime) {
        if (!remindTime) return '--:--';
        return formatTimestamp(remindTime, 'time');
    },
    bindDateChange(e) {
        this.currentDate = e.detail.value;
        this.fetchHistory(this.currentDate);
    },
    fetchHistory(date) {
        const userId = uni.getStorageSync('user')?.id || 1;
        uni.showLoading();
        request({
            url: '/task/daily',
            data: { userId, date }
        }).then(data => {
            this.historyList = data || [];
        }).catch(err => {
            console.error(err);
            this.historyList = [];
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
