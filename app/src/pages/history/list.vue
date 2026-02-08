<template>
  <div class="container">
    <div class="list-container">
      <div 
        class="log-item" 
        v-for="(item, index) in logs" 
        :key="index"
      >
        <div class="log-left">
           <div class="task-title">{{ item.taskTitle }}</div>
           <div class="log-time">{{ formatTime(item.checkTime) }}</div>
        </div>
        <div class="log-right">
           <text v-if="item.status === 1" class="status-tag success">正常</text>
           <text v-else-if="item.status === 2" class="status-tag warning">补卡</text>
        </div>
      </div>
      
      <div class="loading-more" v-if="loading">加载中...</div>
      <div class="no-more" v-if="!hasMore && logs.length > 0">没有更多了</div>
      <div class="empty" v-if="!loading && logs.length === 0">暂无打卡记录</div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { formatTimestamp } from '@/utils/dateUtils';

export default {
  data() {
    return {
      logs: [],
      pageNum: 1,
      pageSize: 20,
      loading: false,
      hasMore: true
    };
  },
  onLoad() {
    this.fetchLogs(true);
  },
  onPullDownRefresh() {
    this.fetchLogs(true);
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.pageNum++;
      this.fetchLogs(false);
    }
  },
  methods: {
    formatTime(time) {
        if (!time) return '';
        return formatTimestamp(time, 'full');
    },
    fetchLogs(refresh) {
      if (refresh) {
          this.pageNum = 1;
          this.hasMore = true;
          this.logs = [];
      }
      
      this.loading = true;
      request({
        url: '/checkIn/logs',
        data: {
            pageNum: this.pageNum,
            pageSize: this.pageSize
        }
      }).then(res => {
        // Response structure: IPage<CheckInLogVO>
        const newLogs = res.records || [];
        if (refresh) {
            this.logs = newLogs;
        } else {
            this.logs = [...this.logs, ...newLogs];
        }
        
        // If we got fewer records than pageSize, it means we reached the end
        if (newLogs.length < this.pageSize) {
            this.hasMore = false;
        }
      }).catch(err => {
        console.error('Fetch logs failed', err);
        uni.showToast({ title: '加载失败', icon: 'none' });
      }).finally(() => {
        this.loading = false;
        uni.stopPullDownRefresh();
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

.log-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02);
}

.task-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.log-time {
  font-size: 24rpx;
  color: #999;
}

.status-tag {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.status-tag.success {
  background-color: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.status-tag.warning {
  background-color: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.loading-more, .no-more, .empty {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 26rpx;
}
</style>
