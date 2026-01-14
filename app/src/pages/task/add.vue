<template>
  <div class="container">
    <div class="form-group">
      <div class="label">为谁设置：</div>
      <picker @change="bindPickerChange" :value="index" :range="array" range-key="name">
         <div class="picker-input">{{ array[index] ? array[index].name : '请选择' }}</div>
      </picker>
    </div>

    <div class="form-group">
      <div class="label">任务内容：</div>
      <input class="input" placeholder="例如：晚上10点睡觉" v-model="content" />
    </div>

    <div class="form-group">
      <div class="label">提醒时间：</div>
      <picker mode="time" :value="time" @change="bindTimeChange">
        <div class="picker-input">{{ time }}</div>
      </picker>
    </div>

    <div class="form-group">
      <div class="label">重复：</div>
       <picker @change="bindRepeatChange" :value="repeatIndex" :range="repeats">
         <div class="picker-input">{{ repeats[repeatIndex] }}</div>
      </picker>
    </div>

    <button class="save-btn" @click="submit">保存任务</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      array: [{name: '宝贝', id: 1}, {name: '老妈', id: 2}],
      index: 0,
      content: '',
      time: '12:00',
      repeats: ['每天', '工作日', '周末'],
      repeatIndex: 0
    }
  },
  methods: {
    bindPickerChange(e) {
      this.index = e.detail.value;
    },
    bindTimeChange(e) {
      this.time = e.detail.value;
    },
    bindRepeatChange(e) {
      this.repeatIndex = e.detail.value;
    },
    submit() {
       uni.showToast({ title: '保存成功' });
       setTimeout(() => uni.navigateBack(), 1500);
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
.save-btn {
  background-color: #68FFB4;
  color: #333;
  border-radius: 50rpx;
  margin-top: 80rpx;
  font-weight: bold;
}
</style>
