package com.dingdong.controller.wechat;

import com.dingdong.common.Result;
import com.dingdong.service.wechat.WechatApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信API控制器
 * 提供动态消息相关接口
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
            String activityId = wechatApiService.createActivityId(null);
            Map<String, String> result = new HashMap<>();
            result.put("activityId", activityId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "创建活动ID失败: " + e.getMessage());
        }
    }

    /**
     * 创建并关联动态消息活动ID到关系记录
     * 
     * @param relationId 关系记录ID（可选，如果已有关系记录）
     * @return 包含activityId的响应
     */
    @PostMapping("/createActivityIdForRelation")
    public Result<Map<String, String>> createActivityIdForRelation(
            @RequestParam(required = false) Long relationId) {
        try {
            String activityId = wechatApiService.createActivityId(null);

            // 如果有关系ID，缓存映射关系
            if (relationId != null) {
                wechatApiService.cacheActivityId(relationId, activityId);
            }

            Map<String, String> result = new HashMap<>();
            result.put("activityId", activityId);
            return Result.success(result);
        } catch (Exception e) {
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

            Map<String, Object> templateInfo = new HashMap<>();
            Map<String, Object> parameterList = new HashMap<>();

            if ("accepted".equals(status)) {
                // 已接受 - 使用模板1的状态
                parameterList.put("member_count", "2");
                parameterList.put("room_limit", "2");
            } else {
                // 已拒绝
                parameterList.put("member_count", "1");
                parameterList.put("room_limit", "2");
            }

            templateInfo.put("parameter_list", new Object[] {
                    Map.of("name", "member_count", "value", parameterList.get("member_count")),
                    Map.of("name", "room_limit", "value", parameterList.get("room_limit"))
            });

            wechatApiService.setUpdatableMsg(activityId, targetState, templateInfo);

            return Result.success(true);
        } catch (Exception e) {
            return Result.error(500, "更新动态消息失败: " + e.getMessage());
        }
    }
}
