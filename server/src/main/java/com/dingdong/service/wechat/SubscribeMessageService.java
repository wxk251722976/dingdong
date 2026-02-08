package com.dingdong.service.wechat;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.hutool.core.util.StrUtil;
import com.dingdong.config.WxMaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信订阅消息服务
 * 使用 WxJava SDK 发送小程序订阅消息通知
 * 
 * 订阅消息使用说明：
 * 1. 在微信公众平台(mp.weixin.qq.com)选择合适的消息模板
 * 2. 将模板ID配置到 application.yml 中
 * 3. 用户需要在小程序中授权订阅才能收到消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeMessageService {

    private final WxMaService wxMaService;
    private final WxMaConfig wxMaConfig;

    /**
     * 发送订阅消息
     *
     * @param toOpenId   接收者的openid
     * @param templateId 模板ID
     * @param data       模板数据
     * @param page       点击跳转的小程序页面路径
     * @return 是否发送成功
     */
    public boolean sendSubscribeMessage(String toOpenId, String templateId,
            List<WxMaSubscribeMessage.MsgData> data, String page) {
        // 检查模板ID是否有效
        if (templateId == null || templateId.isEmpty() || templateId.startsWith("YOUR_")) {
            log.warn("订阅消息模板ID未配置或无效: templateId={}", templateId);
            return false;
        }

        try {
            WxMaSubscribeMessage message = WxMaSubscribeMessage.builder()
                    .toUser(toOpenId)
                    .templateId(templateId)
                    .page(page)
                    .data(data)
                    .build();

            wxMaService.getMsgService().sendSubscribeMsg(message);
            log.info("订阅消息发送成功, toOpenId={}, templateId={}", toOpenId, templateId);
            return true;
        } catch (WxErrorException e) {
            if (e.getError().getErrorCode() == 43101) {
                // 用户未订阅该模板消息
                log.warn("用户未订阅该模板消息: toOpenId={}, templateId={}", toOpenId, templateId);
            } else {
                log.error("订阅消息发送失败: errcode={}, errmsg={}",
                        e.getError().getErrorCode(), e.getError().getErrorMsg());
            }
            return false;
        } catch (Exception e) {
            log.error("发送订阅消息异常: toOpenId={}, templateId={}", toOpenId, templateId, e);
            return false;
        }
    }

    /**
     * 发送叮咚提醒消息给被叮咚者
     * 当到达叮咚时间时调用
     *
     * @param toOpenId       被叮咚者的openid
     * @param taskTitle      任务标题
     * @param remindTime     提醒时间
     * @param supervisorName 监督者名称
     */
    public void sendRemindMessage(String toOpenId, String taskTitle, LocalDateTime remindTime, String supervisorName) {
        String templateId = wxMaConfig.getSubscribeMessage().getRemindCheckin();

        // Template: 签到提醒 (Task Reminder)
        // thing1 - Activity Name (Task Title)
        // time3 - Check-in Time
        // thing8 - Reminder
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("time3",
                remindTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing8", "任务将在30分钟后开始，请做好准备"));

        sendSubscribeMessage(toOpenId, templateId, data, "pages/home/index");
    }

    /**
     * 发送打卡完成通知给监督者
     * 当被监督者在规定时间内完成打卡时调用
     *
     * @param toOpenId       监督者的openid
     * @param supervisedName 被监督者名称
     * @param taskTitle      任务标题
     * @param checkTime      打卡时间
     * @param isOnTime       是否准时（true=准时，false=迟到但在30分钟内）
     */
    public void sendCheckInCompleteMessage(String toOpenId, String supervisedName, String taskTitle,
            LocalDateTime checkTime, boolean isOnTime) {
        String templateId = wxMaConfig.getSubscribeMessage().getCheckinComplete();

        // Template: 签到提醒 (Check-in Success)
        // thing1 - Activity Name
        // thing2 - Check-in Reward (Used for Status/Result)
        // time3 - Check-in Time
        // thing8 - Reminder
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("thing2", isOnTime ? "打卡成功" : "延迟打卡"));
        data.add(new WxMaSubscribeMessage.MsgData("time3",
                checkTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing8", "用户 " + truncateValue(supervisedName) + " 已完成打卡"));

        sendSubscribeMessage(toOpenId, templateId, data, "pages/supervisor/index");
    }

    /**
     * 发送漏打卡通知给监督者
     * 当超过叮咚时间30分钟仍未打卡时调用
     *
     * @param toOpenId       监督者的openid
     * @param supervisedName 被监督者名称
     * @param taskTitle      任务标题
     * @param remindTime     原定提醒时间
     */
    public void sendMissedCheckInMessage(String toOpenId, String supervisedName, String taskTitle,
            LocalDateTime remindTime) {
        String templateId = wxMaConfig.getSubscribeMessage().getMissedCheckin();

        // Template: 签到提醒 (Using same template for Missed Check-in)
        // thing1 - Activity Name
        // thing2 - Check-in Reward (Used for Status)
        // time3 - Check-in Time
        // thing8 - Reminder
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("thing2", "未打卡"));
        data.add(new WxMaSubscribeMessage.MsgData("time3",
                remindTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing8", "用户 " + truncateValue(supervisedName) + " 已超时30分钟未打卡"));

        sendSubscribeMessage(toOpenId, templateId, data, "pages/supervisor/index");
    }

    /**
     * 发送解绑通知
     * 模板ID: 461
     * 标题: 预约通知
     * 
     * @param openid        接收者OpenID
     * @param initiatorName 发起人昵称
     * @param reason        解绑原因
     * @param relationName  关系名称
     */
    public void sendUnbindMessage(String openid, String initiatorName, String reason, String relationName) {
        String templateId = wxMaConfig.getSubscribeMessage().getUnbind();

        // 模板ID: 461
        // 预约人: {{name1.DATA}} -> initiatorName
        // 取消原因: {{thing72.DATA}} -> reason
        // 温馨提示: {{thing8.DATA}} -> "xx用户解除xx关系，24小时后生效"

        List<WxMaSubscribeMessage.MsgData> listData = new ArrayList<>();
        listData.add(new WxMaSubscribeMessage.MsgData("name1", truncateValue(initiatorName)));
        listData.add(new WxMaSubscribeMessage.MsgData("thing72", truncateValue(reason)));
        // 缩短文案以符合thing字段30字限制
        String tip = String.format("%s用户解除%s关系，24小时后生效", truncateValue(initiatorName), truncateValue(relationName));
        listData.add(new WxMaSubscribeMessage.MsgData("thing8", truncateValue(tip)));

        sendSubscribeMessage(openid, templateId, listData, "pages/index/index");
    }

    /**
     * 发送打卡提醒消息（新任务设置通知）
     * 当有人给用户设置新任务后调用
     * 使用模板518: 打卡提醒
     *
     * @param toOpenId    任务执行者的openid
     * @param taskTitle   任务标题（活动名称）
     * @param remindTime  打卡时间
     * @param creatorName 任务创建者名称
     */
    public void sendTaskReminderMessage(String toOpenId, String taskTitle,
            LocalDateTime remindTime, String creatorName) {
        String templateId = wxMaConfig.getSubscribeMessage().getTaskReminder();

        List<WxMaSubscribeMessage.MsgData> data = buildTaskReminderData(taskTitle, remindTime, creatorName);
        sendSubscribeMessage(toOpenId, templateId, data, "pages/home/index");
    }

    /**
     * 构建打卡提醒消息数据
     */
    private List<WxMaSubscribeMessage.MsgData> buildTaskReminderData(String taskTitle,
            LocalDateTime remindTime, String creatorName) {
        // 模板518字段: thing1-活动名称, time2-打卡时间, thing3-备注
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("time2",
                remindTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing3",
                truncateValue(creatorName + "用户给你设置了任务")));
        return data;
    }

    /**
     * 截断字符串以符合微信模板限制
     * 微信订阅消息的 thing 类型字段限制为30个字符
     */
    private String truncateValue(String value) {
        return StrUtil.subPre(value, 30);
    }
}
