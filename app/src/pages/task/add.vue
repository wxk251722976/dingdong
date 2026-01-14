<template>
  <div class="container">
    <div class="form-group">
      <div class="label">为谁设置：</div>
      <picker @change="bindPickerChange" :value="selectedIndex" :range="supervisedUsers" range-key="nickname">
         <div class="picker-input">{{ supervisedUsers[selectedIndex] ? supervisedUsers[selectedIndex].nickname : '请选择' }}</div>
      </picker>
    </div>

    <div class="form-group">
      <div class="label">任务内容：</div>
      <input class="input" placeholder="例如：晚上10点睡觉" v-model="content" />
    </div>

    <!-- 任务类型选择 -->
    <div class="form-group">
      <div class="label">任务类型：</div>
      <div class="type-tabs">
        <div class="type-tab" :class="{ active: repeatType === 1 }" @click="repeatType = 1">
          每天
        </div>
        <div class="type-tab" :class="{ active: repeatType === 2 }" @click="repeatType = 2">
          工作日
        </div>
        <div class="type-tab" :class="{ active: repeatType === 3 }" @click="repeatType = 3">
          周末
        </div>
        <div class="type-tab" :class="{ active: repeatType === 0 }" @click="repeatType = 0">
          单次
        </div>
      </div>
    </div>

    <!-- 重复任务设置 -->
    <template v-if="repeatType !== 0">
      <div class="form-group">
        <div class="label">提醒时间：</div>
        <picker mode="time" :value="time" @change="bindTimeChange">
          <div class="picker-input">{{ time }}</div>
        </picker>
      </div>
    </template>

    <!-- 单次任务设置 -->
    <template v-if="repeatType === 0">
      <div class="form-group">
        <div class="label">指定日期：</div>
        <picker mode="date" :value="onceDate" @change="bindDateChange" :start="today">
          <div class="picker-input">{{ onceDate }}</div>
        </picker>
      </div>

      <div class="form-group">
        <div class="label">提醒时间：</div>
        <picker mode="time" :value="onceTime" @change="bindOnceTimeChange">
          <div class="picker-input">{{ onceTime }}</div>
        </picker>
      </div>
    </template>

    <button class="save-btn" @click="submit">保存任务</button>
  </div>
</template>

<script>
import request from '@/utils/request';
import { RepeatType } from '@/utils/constants';

export default {
  data() {
    const now = new Date();
    const today = now.toISOString().split('T')[0];
    
    return {
      supervisedUsers: [],
      selectedIndex: 0,
      content: '',
      repeatType: RepeatType.DAILY.code,  // 默认每天
      
      // 重复任务
      time: '12:00',
      
      // 单次任务
      today: today,
      onceDate: today,
      onceTime: '12:00'
    }
  },
  onLoad() {
    this.fetchSupervisedUsers();
  },
  methods: {
    async fetchSupervisedUsers() {
      try {
        const userId = uni.getStorageSync('user')?.id;
        if (!userId) return;
        
        const relations = await request({
          url: '/relation/myRelations',
          data: { userId }
        });
        
        this.supervisedUsers = relations
          .filter(r => r.childId === userId && r.status === 1)
          .map(r => ({
            id: r.elderId,
            nickname: r.elderNickname || `用户${r.elderId}`
          }));
      } catch (e) {
        console.error('获取监督列表失败', e);
      }
    },
    bindPickerChange(e) {
      this.selectedIndex = e.detail.value;
    },
    bindTimeChange(e) {
      this.time = e.detail.value;
    },
    bindDateChange(e) {
      this.onceDate = e.detail.value;
    },
    bindOnceTimeChange(e) {
      this.onceTime = e.detail.value;
    },
    async submit() {
      if (!this.content.trim()) {
        uni.showToast({ title: '请输入任务内容', icon: 'none' });
        return;
      }
      if (this.supervisedUsers.length === 0) {
        uni.showToast({ title: '请先添加监督对象', icon: 'none' });
        return;
      }
      
      const creatorId = uni.getStorageSync('user')?.id;
      const targetUser = this.supervisedUsers[this.selectedIndex];
      
      let remindTime;
      if (this.repeatType === RepeatType.ONCE.code) {
        // 单次任务：完整日期时间
        remindTime = `${this.onceDate}T${this.onceTime}:00`;
      } else {
        // 重复任务：只有时间部分，日期使用今天作为占位
        const today = new Date().toISOString().split('T')[0];
        remindTime = `${today}T${this.time}:00`;
      }
      
      try {
        uni.showLoading({ title: '保存中...' });
        await request({
          url: '/task/add',
          method: 'POST',
          data: {
            creatorId,
            userId: targetUser.id,
            title: this.content,
            remindTime,
            repeatType: this.repeatType
          }
        });
        uni.hideLoading();
        uni.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => uni.navigateBack(), 1500);
      } catch (e) {
        uni.hideLoading();
        console.error('创建任务失败', e);
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 40rpx;
  background-color: #F8F8F8;
  min-height: 100vh;
}
.form-group {
  margin-bottom: 40rpx;
}
.label {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 20rpx;
}
.picker-input, .input {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  color: #333;
}

.type-tabs {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}
.type-tab {
  flex: 1;
  min-width: 140rpx;
  text-align: center;
  padding: 20rpx 16rpx;
  background-color: #fff;
  border-radius: 12rpx;
  color: #666;
  border: 2rpx solid #eee;
  font-size: 26rpx;
}
.type-tab.active {
  background-color: #68FFB4;
  color: #333;
  border-color: #68FFB4;
  font-weight: bold;
}

.save-btn {
  background-color: #68FFB4;
  color: #333;
  border-radius: 50rpx;
  margin-top: 80rpx;
  font-weight: bold;
}
</style>
