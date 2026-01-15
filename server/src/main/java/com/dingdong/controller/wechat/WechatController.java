package com.dingdong.controller.wechat;

import com.dingdong.common.Result;
import com.dingdong.service.wechat.WechatApiService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信API控制器
 * 使用 WxJava SDK 提供动态消息相关接口
 */
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WechatController {

    private final WechatApiService wechatApiService;

    /**
     * 创建动态消息活动ID
     * 用于小程序分享动态消息时使用
     * 
     * @return 包含activityId的响应
     */
    @PostMapping("/createActivityId")
    public Result<Map<String, String>> createActivityId() {
        try {
            String activityId = wechatApiService.createActivityId();
            Map<String, String> result = new HashMap<>();
            result.put("activityId", activityId);
            return Result.success(result);
        } catch (WxErrorException e) {
            return Result.error(500, "创建活动ID失败: " + e.getMessage());
        }
    }

    /**
     * 更新动态消息状态
     * 
     * @param requestBody 包含 activityId 和 status (accepted/rejected)
     * @return 是否成功
     */
    @PostMapping("/updateDynamicMsg")
    public Result<Boolean> updateDynamicMsg(@RequestBody Map<String, String> requestBody) {
        try {
            String activityId = requestBody.get("activityId");
            String status = requestBody.get("status");

            if (activityId == null || activityId.isEmpty()) {
                return Result.error(400, "activityId不能为空");
            }

            // 根据状态设置动态消息
            // target_state: 0-未开始, 1-进行中, 2-已结束
            int targetState = 2; // 已结束

            String memberCount;
            String roomLimit = "2";

            if ("accepted".equals(status)) {
                // 已接受
                memberCount = "2";
            } else {
                // 已拒绝
                memberCount = "1";
            }

            wechatApiService.setUpdatableMsg(activityId, targetState, memberCount, roomLimit);

            return Result.success(true);
        } catch (WxErrorException e) {
            return Result.error(500, "更新动态消息失败: " + e.getMessage());
        }
    }
}
