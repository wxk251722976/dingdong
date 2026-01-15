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

        // 模板数据格式参考:
        // thing1 - 任务名称
        // time2 - 提醒时间
        // thing3 - 创建者/监督者
        // thing4 - 备注
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("time2",
                remindTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing3", truncateValue(supervisorName)));
        data.add(new WxMaSubscribeMessage.MsgData("thing4", "记得按时完成叮咚打卡哦~"));

        sendSubscribeMessage(toOpenId, templateId, data, "pages/supervised/index");
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

        // 模板数据格式参考:
        // thing1 - 用户名称
        // thing2 - 任务名称
        // time3 - 完成时间
        // thing4 - 状态/备注
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(supervisedName)));
        data.add(new WxMaSubscribeMessage.MsgData("thing2", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("time3",
                checkTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing4", isOnTime ? "准时完成 ✓" : "已完成(稍有延迟)"));

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

        // 模板数据格式参考:
        // thing1 - 用户名称
        // thing2 - 任务名称
        // time3 - 时间
        // thing4 - 状态/备注
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(new WxMaSubscribeMessage.MsgData("thing1", truncateValue(supervisedName)));
        data.add(new WxMaSubscribeMessage.MsgData("thing2", truncateValue(taskTitle)));
        data.add(new WxMaSubscribeMessage.MsgData("time3",
                remindTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.add(new WxMaSubscribeMessage.MsgData("thing4", "已超时30分钟未打卡 ⚠"));

        sendSubscribeMessage(toOpenId, templateId, data, "pages/supervisor/index");
    }

    /**
     * 截断字符串以符合微信模板限制
     * 微信订阅消息的 thing 类型字段限制为20个字符
     */
    private String truncateValue(String value) {
        return StrUtil.subSufByLength(value, 20);
    }
}
