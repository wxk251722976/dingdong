<template>
  <div class="container">
    <!-- 用户等级卡片 -->
    <div class="level-card">
      <div class="level-info">
        <div class="level-badge">{{ stats.levelName || '普通用户' }}</div>
        <div class="user-name">{{ stats.nickname || '用户' }}</div>
      </div>
      <div class="streak-info">
        <div class="streak-number">{{ stats.streakDays || 0 }}</div>
        <div class="streak-label">连续打卡天</div>
      </div>
    </div>
    
    <!-- 核心数据卡片 -->
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-value">{{ stats.supervisedCount || 0 }}/{{ stats.maxSupervisedCount || 3 }}</div>
        <div class="stat-label">监督人数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.supervisorCount || 0 }}</div>
        <div class="stat-label">监督我的</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.totalCheckInCount || 0 }}</div>
        <div class="stat-label">总打卡次</div>
      </div>
      <div class="stat-item">
        <div class="stat-value green">{{ stats.monthCompletedCount || 0 }}</div>
        <div class="stat-label">本月完成</div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="chart-card">
      <div class="card-header">
        <text class="title">近7天打卡统计</text>
      </div>
      <div class="chart-container">
        <div class="bar-chart">
          <div 
            class="bar-item" 
            v-for="(count, index) in stats.weeklyCompletedCounts || []" 
            :key="index"
          >
            <div class="bar-fill" :style="{ height: getBarHeight(count) + 'rpx' }"></div>
            <div class="bar-value">{{ count }}</div>
            <div class="bar-label">{{ (stats.weeklyLabels || [])[index] }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 今日概览 -->
    <div class="summary-card">
      <div class="card-header">
        <text class="title">今日概览</text>
      </div>
      <div class="summary-content">
        <div class="summary-item">
          <div class="summary-icon green">✅</div>
          <div class="summary-info">
            <div class="summary-value">{{ stats.todayCompletedCount || 0 }}</div>
            <div class="summary-label">已完成</div>
          </div>
        </div>
        <div class="summary-item">
          <div class="summary-icon orange">⏳</div>
          <div class="summary-info">
            <div class="summary-value">{{ (stats.todayTaskCount || 0) - (stats.todayCompletedCount || 0) }}</div>
            <div class="summary-label">待完成</div>
          </div>
        </div>
        <div class="summary-item">
          <div class="summary-icon red">⚠️</div>
          <div class="summary-info">
            <div class="summary-value">{{ stats.todayMissedCount || 0 }}</div>
            <div class="summary-label">已错过</div>
          </div>
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
      stats: {}
    };
  },
  onShow() {
    this.fetchStats();
  },
  methods: {
    async fetchStats() {
      try {
        const stats = await request({
          url: '/user/stats'
        });
        this.stats = stats || {};
      } catch (e) {
        console.error('获取统计数据失败', e);
      }
    },
    getBarHeight(count) {
      // 计算柱状图高度，最大200rpx
      const max = Math.max(...(this.stats.weeklyCompletedCounts || [1]), 1);
      return Math.max(20, (count / max) * 200);
    }
  }
}
</script>

<style scoped>
.container {
  background-color: #F8F8F8;
  padding: 30rpx;
  box-sizing: border-box;
}

.level-card {
  background: linear-gradient(135deg, #68FFB4 0%, #4ECDC4 100%);
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(104, 255, 180, 0.3);
}

.level-info {
  flex: 1;
}

.level-badge {
  display: inline-block;
  background-color: rgba(255,255,255,0.3);
  color: #fff;
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-bottom: 12rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.streak-info {
  text-align: center;
  background-color: rgba(255,255,255,0.2);
  padding: 20rpx 30rpx;
  border-radius: 16rpx;
}

.streak-number {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.streak-label {
  font-size: 22rpx;
  color: rgba(255,255,255,0.8);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.stat-value.green {
  color: #07c160;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.chart-card, .summary-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.card-header {
  margin-bottom: 24rpx;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.chart-container {
  padding: 20rpx 0;
}

.bar-chart {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 280rpx;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 8rpx;
}

.bar-fill {
  width: 100%;
  max-width: 60rpx;
  background: linear-gradient(180deg, #68FFB4 0%, #4ECDC4 100%);
  border-radius: 8rpx 8rpx 0 0;
  transition: height 0.3s;
}

.bar-value {
  font-size: 20rpx;
  color: #333;
  margin-top: 8rpx;
  font-weight: bold;
}

.bar-label {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
}

.summary-content {
  display: flex;
  justify-content: space-around;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.summary-icon {
  font-size: 40rpx;
}

.summary-icon.green { background-color: rgba(7, 193, 96, 0.1); padding: 16rpx; border-radius: 50%; }
.summary-icon.orange { background-color: rgba(255, 152, 0, 0.1); padding: 16rpx; border-radius: 50%; }
.summary-icon.red { background-color: rgba(250, 81, 81, 0.1); padding: 16rpx; border-radius: 50%; }

.summary-info {
  text-align: left;
}

.summary-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.summary-label {
  font-size: 22rpx;
  color: #999;
}
</style>
