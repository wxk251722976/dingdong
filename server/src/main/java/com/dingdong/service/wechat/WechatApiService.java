package com.dingdong.service.wechat;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dingdong.config.WxMaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信API服务
 * 使用 WxJava SDK 实现微信小程序相关API调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatApiService {

    private final WxMaService wxMaService;
    private final WxMaConfig wxMaConfig;

    /**
     * 通过 code 获取用户 session 信息（包含 openid）
     * 
     * @param code 微信登录 code
     * @return session 信息
     */
    public WxMaJscode2SessionResult getSessionInfo(String code) throws WxErrorException {
        return wxMaService.getUserService().getSessionInfo(code);
    }

    /**
     * 获取用户 openid
     * 
     * @param code 微信登录 code
     * @return openid
     */
    public String getOpenid(String code) throws WxErrorException {
        WxMaJscode2SessionResult session = getSessionInfo(code);
        return session.getOpenid();
    }

    /**
     * 发送订阅消息
     * 
     * @param openid     用户 openid
     * @param templateId 模板ID
     * @param page       跳转页面
     * @param data       消息数据
     */
    public void sendSubscribeMessage(String openid, String templateId, String page,
            List<WxMaSubscribeMessage.MsgData> data) throws WxErrorException {
        WxMaSubscribeMessage message = WxMaSubscribeMessage.builder()
                .toUser(openid)
                .templateId(templateId)
                .page(page)
                .data(data)
                .build();

        wxMaService.getMsgService().sendSubscribeMsg(message);
        log.info("订阅消息发送成功: openid={}, templateId={}", openid, templateId);
    }

    /**
     * 发送叮咚提醒消息
     * 
     * @param openid     用户 openid
     * @param taskTitle  任务标题
     * @param remindTime 提醒时间
     */
    public void sendRemindMessage(String openid, String taskTitle, String remindTime) {
        try {
            String templateId = wxMaConfig.getSubscribeMessage().getRemindCheckin();
            List<WxMaSubscribeMessage.MsgData> data = List.of(
                    new WxMaSubscribeMessage.MsgData("thing1", taskTitle),
                    new WxMaSubscribeMessage.MsgData("time2", remindTime));
            sendSubscribeMessage(openid, templateId, "/pages/home/index", data);
        } catch (WxErrorException e) {
            log.error("发送叮咚提醒消息失败: openid={}, error={}", openid, e.getMessage());
        }
    }

    /**
     * 发送打卡完成通知
     * 
     * @param openid    用户 openid
     * @param taskTitle 任务标题
     * @param checkTime 打卡时间
     */
    public void sendCheckinCompleteMessage(String openid, String taskTitle, String checkTime) {
        try {
            String templateId = wxMaConfig.getSubscribeMessage().getCheckinComplete();
            List<WxMaSubscribeMessage.MsgData> data = List.of(
                    new WxMaSubscribeMessage.MsgData("thing1", taskTitle),
                    new WxMaSubscribeMessage.MsgData("time2", checkTime));
            sendSubscribeMessage(openid, templateId, "/pages/home/index", data);
        } catch (WxErrorException e) {
            log.error("发送打卡完成通知失败: openid={}, error={}", openid, e.getMessage());
        }
    }

    /**
     * 发送漏打卡通知
     * 
     * @param openid    用户 openid
     * @param taskTitle 任务标题
     * @param missDate  漏打卡日期
     */
    public void sendMissedCheckinMessage(String openid, String taskTitle, String missDate) {
        try {
            String templateId = wxMaConfig.getSubscribeMessage().getMissedCheckin();
            List<WxMaSubscribeMessage.MsgData> data = List.of(
                    new WxMaSubscribeMessage.MsgData("thing1", taskTitle),
                    new WxMaSubscribeMessage.MsgData("date2", missDate));
            sendSubscribeMessage(openid, templateId, "/pages/home/index", data);
        } catch (WxErrorException e) {
            log.error("发送漏打卡通知失败: openid={}, error={}", openid, e.getMessage());
        }
    }

    /**
     * 创建动态消息活动ID
     * 用于转发动态消息时声明消息类型
     * 
     * @return activityId
     */
    public String createActivityId() throws WxErrorException {
        // WxJava 暂不直接支持创建动态消息活动ID，使用底层HTTP调用
        String result = wxMaService.post(
                "https://api.weixin.qq.com/cgi-bin/message/wxopen/activityid/create",
                "{}");
        log.info("创建activityId响应: {}", result);

        // 解析返回的 activity_id
        JSONObject json = JSONUtil.parseObj(result);
        if (json.getInt("errcode", 0) == 0) {
            return json.getStr("activity_id");
        }
        throw new WxErrorException("创建activityId失败: " + json.getStr("errmsg"));
    }

    /**
     * 更新动态消息状态
     * 
     * @param activityId  活动ID
     * @param targetState 目标状态: 0-未开始, 1-进行中, 2-已结束
     * @param memberCount 成员数量
     * @param roomLimit   房间限制
     */
    public void setUpdatableMsg(String activityId, int targetState, String memberCount, String roomLimit)
            throws WxErrorException {
        String requestBody = JSONUtil.toJsonStr(new java.util.HashMap() {
            {
                put("activity_id", activityId);
                put("target_state", targetState);
                put("template_info", new HashMap<String, Object>() {
                    {
                        put("parameter_list", new Object[] {
                                Map.of("name", "member_count", "value", memberCount),
                                Map.of("name", "room_limit", "value", roomLimit)
                        });
                    }
                });
            }
        });

        String result = wxMaService.post(
                "https://api.weixin.qq.com/cgi-bin/message/wxopen/updatablemsg/send",
                requestBody);
        log.info("更新动态消息响应: {}", result);

        JSONObject json = JSONUtil.parseObj(result);
        if (json.getInt("errcode", 0) != 0) {
            log.error("更新动态消息失败: {}", result);
        }
    }
}
