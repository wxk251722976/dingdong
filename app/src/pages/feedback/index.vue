<template>
  <div class="container">
    <div class="feedback-card">
      <div class="card-header">
        <text class="title">意见反馈</text>
        <text class="remaining">今日剩余：{{ remainingCount }} 次</text>
      </div>
      
      <textarea 
        class="feedback-input"
        placeholder="请输入您的意见或建议，我们会认真阅读..."
        v-model="content"
        :maxlength="500"
      ></textarea>
      <div class="char-count">{{ content.length }}/500</div>
      
      <input 
        class="contact-input"
        placeholder="联系方式（选填）"
        v-model="contact"
      />
      
      <button 
        class="submit-btn"
        :disabled="!content.trim() || remainingCount <= 0"
        @click="submitFeedback"
      >
        提交反馈
      </button>
    </div>
    
    <div class="help-section">
      <div class="card-header">
        <text class="title">常见问题</text>
      </div>
      
      <div class="faq-item" v-for="(item, index) in faqList" :key="index" @click="toggleFaq(index)">
        <div class="faq-question">
          <text>{{ item.question }}</text>
          <text class="arrow" :class="{ expanded: item.expanded }">></text>
        </div>
        <div class="faq-answer" v-if="item.expanded">{{ item.answer }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { formatTimestamp } from '@/utils/dateUtils';

export default {
  data() {
    return {
      content: '',
      contact: '',
      remainingCount: 3,
      faqList: [
        { question: '如何添加监督对象？', answer: '在"我的"页面点击"关系管理"，然后点击右下角的"+"按钮发送邀请链接给对方。', expanded: false },
        { question: '任务提醒不准时怎么办？', answer: '请确保微信小程序后台运行权限已开启，并允许小程序发送通知。', expanded: false },
        { question: '如何修改已创建的任务？', answer: '在"任务管理"中点击对应用户，进入任务列表后点击任务即可编辑。', expanded: false },
        { question: '打卡时间过了还能补打卡吗？', answer: '超过提醒时间30分钟后将无法打卡，任务会标记为"已错过"。', expanded: false },
        { question: '如何升级为VIP用户？', answer: '目前VIP功能暂未开放，敬请期待后续更新。', expanded: false }
      ]
    };
  },
  onShow() {
    this.fetchRemainingCount();
  },
  methods: {
    async fetchRemainingCount() {
      try {
        const res = await request({
          url: '/user/feedback/remaining'
        });
        this.remainingCount = res.remaining || 0;
      } catch (e) {
        console.error('获取反馈次数失败', e);
      }
    },
    async submitFeedback() {
      if (!this.content.trim()) {
        uni.showToast({ title: '请输入反馈内容', icon: 'none' });
        return;
      }
      
      try {
        uni.showLoading({ title: '提交中...' });
        await request({
          url: '/user/feedback/submit',
          method: 'POST',
          data: {
            content: this.content,
            contact: this.contact
          }
        });
        uni.hideLoading();
        uni.showToast({ title: '反馈已提交', icon: 'success' });
        this.content = '';
        this.contact = '';
        this.fetchRemainingCount();
      } catch (e) {
        uni.hideLoading();
        console.error('提交反馈失败', e);
      }
    },
    formatTime(timestamp) {
      return formatTimestamp(timestamp, 'datetime');
    },
    toggleFaq(index) {
      this.faqList[index].expanded = !this.faqList[index].expanded;
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

.feedback-card, .help-section {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.remaining {
  font-size: 24rpx;
  color: #999;
}

.feedback-input {
  width: 100%;
  height: 200rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 10rpx;
}

.contact-input {
  width: 100%;
  height: 80rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  margin-top: 20rpx;
  box-sizing: border-box;
}

.submit-btn {
  margin-top: 30rpx;
  background-color: #68FFB4;
  color: #333;
  border-radius: 50rpx;
  font-weight: bold;
  font-size: 30rpx;
}

.submit-btn[disabled] {
  background-color: #ccc;
  color: #999;
}

.feedback-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.feedback-item:last-child {
  border-bottom: none;
}

.feedback-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.feedback-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}

.time {
  font-size: 22rpx;
  color: #999;
}

.status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.status.pending {
  color: #FF9800;
  background-color: rgba(255, 152, 0, 0.1);
}

.status.resolved {
  color: #07c160;
  background-color: rgba(7, 193, 96, 0.1);
}

.faq-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
  color: #333;
}

.arrow {
  color: #999;
  transition: transform 0.3s;
}

.arrow.expanded {
  transform: rotate(90deg);
}

.faq-answer {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  padding-left: 20rpx;
  border-left: 4rpx solid #68FFB4;
}
</style>
