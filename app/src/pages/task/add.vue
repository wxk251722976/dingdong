<template>
  <div class="container">
    <div class="form-group" v-if="!isEditMode">
      <div class="label">为谁设置：</div>
      <picker @change="bindPickerChange" :value="selectedIndex" :range="supervisedUsers" range-key="nickname" :disabled="preSelectUserId">
         <div class="picker-input">{{ supervisedUsers[selectedIndex] ? supervisedUsers[selectedIndex].nickname : '请选择' }}</div>
      </picker>
    </div>
    
    <div class="form-group" v-else>
      <div class="label">任务对象：</div>
      <div class="picker-input readonly">{{ targetUserName }}</div>
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

    <button class="save-btn" @click="submit">{{ isEditMode ? '保存修改' : '保存任务' }}</button>
    
    <button v-if="isEditMode" class="delete-btn" @click="deleteTask">删除任务</button>
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
      // 模式
      isEditMode: false,
      taskId: null,
      preSelectUserId: null,
      targetUserName: '',
      
      // 表单数据
      supervisedUsers: [],
      selectedIndex: 0,
      content: '',
      repeatType: RepeatType.DAILY.code,
      
      // 重复任务
      time: '12:00',
      
      // 单次任务
      today: today,
      onceDate: today,
      onceTime: '12:00'
    }
  },
  onLoad(options) {
    if (options.mode === 'edit' && options.taskId) {
      this.isEditMode = true;
      this.taskId = options.taskId;
      this.fetchTaskDetail();
    } else {
      if (options.preSelectUserId) {
        this.preSelectUserId = options.preSelectUserId;
      }
      this.fetchSupervisedUsers();
    }
  },
  methods: {
    async fetchTaskDetail() {
      try {
        uni.showLoading({ title: '加载中...' });
        const task = await request({
          url: '/task/detail',
          data: { taskId: this.taskId }
        });
        
        if (task) {
          this.content = task.title || '';
          this.repeatType = task.repeatType;
          this.targetUserName = task.userName || '用户';
          
          if (task.remindTime) {
            const [datePart, timePart] = task.remindTime.split('T');
            const timeStr = timePart ? timePart.substring(0, 5) : '12:00';
            
            if (this.repeatType === RepeatType.ONCE.code) {
              this.onceDate = datePart;
              this.onceTime = timeStr;
            } else {
              this.time = timeStr;
            }
          }
        }
      } catch (e) {
        console.error('获取任务详情失败', e);
        uni.showToast({ title: '加载失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    async fetchSupervisedUsers() {
      try {
        const user = uni.getStorageSync('user');
        if (!user || !user.id) return;
        
        const users = await request({
          url: '/checkIn/supervised',
          data: { supervisorId: user.id }
        });
        
        if (users && users.length > 0) {
            this.supervisedUsers = users.map(u => ({
                id: u.id,
                nickname: u.nickname || `用户${u.id}`
            }));
            
            // 如果有预选用户，设置选中索引
            if (this.preSelectUserId) {
              const idx = this.supervisedUsers.findIndex(u => String(u.id) === String(this.preSelectUserId));
              if (idx >= 0) {
                this.selectedIndex = idx;
              }
            }
        } else {
             this.supervisedUsers = [];
             uni.showToast({ title: '暂无关联用户，请先添加', icon: 'none' });
        }
        
      } catch (e) {
        console.error('获取监督列表失败', e);
        this.supervisedUsers = [];
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
      
      if (!this.isEditMode && this.supervisedUsers.length === 0) {
        uni.showToast({ title: '请先添加监督对象', icon: 'none' });
        return;
      }
      
      const creatorId = uni.getStorageSync('user')?.id;
      
      let remindTime;
      if (this.repeatType === RepeatType.ONCE.code) {
        remindTime = `${this.onceDate}T${this.onceTime}:00`;
      } else {
        const today = new Date().toISOString().split('T')[0];
        remindTime = `${today}T${this.time}:00`;
      }
      
      try {
        uni.showLoading({ title: '保存中...' });
        
        if (this.isEditMode) {
          // 编辑模式
          await request({
            url: '/task/update',
            method: 'POST',
            data: {
              taskId: this.taskId,
              title: this.content,
              remindTime,
              repeatType: this.repeatType
            }
          });
        } else {
          // 新增模式
          const targetUser = this.supervisedUsers[this.selectedIndex];
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
        }
        
        uni.hideLoading();
        uni.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => uni.navigateBack(), 1500);
      } catch (e) {
        uni.hideLoading();
        console.error('保存任务失败', e);
      }
    },
    deleteTask() {
      uni.showModal({
        title: '确认删除',
        content: '删除后无法恢复，确定要删除这个任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '删除中...' });
              await request({
                url: '/task/delete',
                method: 'POST',
                data: { taskId: this.taskId }
              });
              uni.hideLoading();
              uni.showToast({ title: '删除成功', icon: 'success' });
              setTimeout(() => uni.navigateBack(), 1500);
            } catch (e) {
              uni.hideLoading();
              console.error('删除任务失败', e);
            }
          }
        }
      });
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

.picker-input.readonly {
  background-color: #f5f5f5;
  color: #999;
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

.delete-btn {
  background-color: #fff;
  color: #fa5151;
  border-radius: 50rpx;
  margin-top: 30rpx;
  border: 2rpx solid #fa5151;
}
</style>
