package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.common.constant.UserConstants;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.user.UserStatsDTO;
import com.dingdong.entity.user.UserLevelConfig;
import com.dingdong.service.user.IUserFeedbackService;
import com.dingdong.service.user.IUserLevelConfigService;
import com.dingdong.service.user.IUserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户相关功能控制器
 * 提供反馈、统计、等级配置等接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFeatureController {

    private final IUserFeedbackService feedbackService;
    private final IUserLevelConfigService levelConfigService;
    private final IUserStatsService statsService;

    // ==================== 反馈功能 ====================

    /**
     * 提交反馈
     */
    @PostMapping("/feedback/submit")
    public Result<Boolean> submitFeedback(@RequestBody Map<String, Object> params) {
        Long userId = SystemContextHolder.getUserId();
        String content = (String) params.get("content");
        String contact = (String) params.getOrDefault("contact", null);

        if (content == null || content.trim().isEmpty()) {
            return Result.error("反馈内容不能为空");
        }

        try {
            boolean success = feedbackService.submitFeedback(userId, content, contact);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取今日剩余反馈次数
     */
    @GetMapping("/feedback/remaining")
    public Result<Map<String, Integer>> getRemainingFeedback() {
        Long userId = SystemContextHolder.getUserId();
        int todayCount = feedbackService.getTodayFeedbackCount(userId);
        UserLevelConfig config = levelConfigService.getUserLevelConfig(userId);
        int maxCount = config != null ? config.getMaxFeedbackPerDay() : UserConstants.DEFAULT_MAX_FEEDBACK_PER_DAY;

        return Result.success(Map.of(
                "used", todayCount,
                "remaining", Math.max(0, maxCount - todayCount),
                "max", maxCount));
    }

    // ==================== 等级配置 ====================

    /**
     * 获取我的等级配置
     */
    @GetMapping("/level/myConfig")
    public Result<UserLevelConfig> getMyLevelConfig() {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(levelConfigService.getUserLevelConfig(userId));
    }

    /**
     * 获取所有等级配置
     */
    @GetMapping("/level/all")
    public Result<List<UserLevelConfig>> getAllLevelConfigs() {
        return Result.success(levelConfigService.lambdaQuery()
                .orderByAsc(UserLevelConfig::getSortOrder)
                .list());
    }

    // ==================== 数据统计 ====================

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Result<UserStatsDTO> getUserStats() {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(statsService.getUserStats(userId));
    }
}
