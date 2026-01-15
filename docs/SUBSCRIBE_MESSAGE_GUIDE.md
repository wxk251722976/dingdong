# 微信小程序订阅消息配置指南

## 功能概述

叮咚打卡应用支持以下三种订阅消息通知：

| 消息类型 | 接收者 | 触发时机 |
|---------|-------|---------|
| 叮咚提醒 | 被叮咚者 | 到达设定的叮咚时间时 |
| 打卡完成通知 | 监督者 | 被监督者在规定时间内完成打卡时 |
| 漏打卡通知 | 监督者 | 超过叮咚时间30分钟仍未打卡时 |

## 配置步骤

### 1. 在微信公众平台选择订阅消息模板

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入「功能」→「订阅消息」
3. 点击「公共模板库」，搜索并选择以下类型的模板：

#### 叮咚提醒模板
推荐搜索关键词：任务提醒、待办提醒、打卡提醒

需要包含的字段类型：
- `thing` - 任务名称
- `time` - 提醒时间
- `thing` - 监督者名称
- `thing` - 备注

#### 打卡完成通知模板
推荐搜索关键词：完成通知、任务完成、打卡成功

需要包含的字段类型：
- `thing` - 用户名称
- `thing` - 任务名称
- `time` - 完成时间
- `thing` - 状态说明

#### 漏打卡通知模板
推荐搜索关键词：超时提醒、未完成提醒、异常通知

需要包含的字段类型：
- `thing` - 用户名称
- `thing` - 任务名称
- `time` - 原定时间
- `thing` - 状态说明

### 2. 配置后端模板ID

获取到模板ID后，需要在后端配置文件中设置：

**方式一：修改 application.yml**

```yaml
wechat:
  template:
    remind-checkin: 你的叮咚提醒模板ID
    checkin-complete: 你的打卡完成通知模板ID
    missed-checkin: 你的漏打卡通知模板ID
```

**方式二：通过环境变量配置**

```bash
export WECHAT_TPL_REMIND=你的叮咚提醒模板ID
export WECHAT_TPL_COMPLETE=你的打卡完成通知模板ID
export WECHAT_TPL_MISSED=你的漏打卡通知模板ID
```

### 3. 配置前端模板ID

修改 `app/src/utils/subscribe.js` 文件中的模板ID：

```javascript
const TEMPLATE_IDS = {
  REMIND_CHECKIN: '你的叮咚提醒模板ID',
  CHECKIN_COMPLETE: '你的打卡完成通知模板ID', 
  MISSED_CHECKIN: '你的漏打卡通知模板ID'
};
```

### 4. 根据实际模板调整字段名

由于不同模板的字段名称可能不同（如 `thing1`、`thing2` 等），需要根据你选择的模板调整代码中的字段名。

修改 `server/src/main/java/com/dingdong/service/wechat/SubscribeMessageService.java` 中的字段名：

```java
// 示例：如果你的模板字段是 name1、thing2、time3、content4
data.put("name1", Map.of("value", taskTitle));
data.put("time2", Map.of("value", timeStr));
data.put("thing3", Map.of("value", supervisorName));
data.put("content4", Map.of("value", "备注信息"));
```

## 用户订阅授权

### 授权时机

- **监督者**：首次进入「监督列表」页面时会弹出授权请求
- **被监督者**：首次进入「首页」时会弹出授权请求

### 用户拒绝后如何处理

如果用户拒绝了订阅授权，可以引导用户去小程序设置页开启：

```javascript
import { showSubscribeGuide } from '@/utils/subscribe';

// 在合适的时机调用
showSubscribeGuide();
```

## 注意事项

1. **订阅消息是一次性的**：用户每次订阅只能收到一次通知，如需持续接收需要重新订阅
2. **模板字段限制**：
   - `thing` 类型最多20个字符
   - `time` 类型需要符合时间格式
3. **每次最多请求3个模板**：`requestSubscribeMessage` 一次调用最多传入3个模板ID
4. **开发环境测试**：订阅消息在开发者工具中调试需要开启「不校验合法域名」

## 故障排查

### 消息发送失败常见原因

| 错误码 | 说明 | 解决方案 |
|-------|-----|---------|
| 43101 | 用户未订阅该消息 | 引导用户重新订阅 |
| 40003 | touser字段不正确 | 检查用户openid是否正确 |
| 40037 | template_id不正确 | 检查模板ID配置 |
| 47003 | 模板参数不准确 | 检查data字段名是否与模板一致 |

### 定时任务不执行

1. 确认 `DingDongApplication` 类添加了 `@EnableScheduling` 注解
2. 检查服务器时间是否正确
3. 查看日志中是否有定时任务执行记录

## 相关文件

- 后端订阅消息服务：`server/src/main/java/com/dingdong/service/wechat/SubscribeMessageService.java`
- 后端定时任务：`server/src/main/java/com/dingdong/task/CheckInReminderTask.java`
- 前端订阅工具：`app/src/utils/subscribe.js`
- 配置文件：`server/src/main/resources/application.yml`
