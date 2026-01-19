package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dingdong.common.constant.RelationStatus;
import com.dingdong.common.constant.TaskStatus;
import com.dingdong.dto.user.UserStatsDTO;
import com.dingdong.entity.checkin.CheckInLog;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserLevelConfig;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.checkin.ICheckInLogService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserLevelConfigService;
import com.dingdong.service.user.IUserRelationService;
import com.dingdong.service.user.IUserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户统计服务实现
 */
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements IUserStatsService {

        private final ISysUserService sysUserService;
        private final IUserRelationService relationService;
        private final IUserLevelConfigService levelConfigService;
        private final ICheckInLogService logService;

        @Override
        public UserStatsDTO getUserStats(Long userId) {
                UserStatsDTO stats = new UserStatsDTO();
                stats.setUserId(userId);

                // 用户基本信息
                SysUser user = sysUserService.getById(userId);
                if (user != null) {
                        stats.setNickname(user.getNickname());
                }

                // 等级信息
                UserLevelConfig levelConfig = levelConfigService.getUserLevelConfig(userId);
                stats.setLevelName(levelConfig.getLevelName());
                stats.setMaxSupervisedCount(levelConfig.getMaxSupervisedCount());

                // 关系统计（平等关系，不再区分监督者/被监督者）
                List<UserRelation> allRelations = relationService.getMyRelations(userId);
                // 平等关系下，统计已接受的关系数量
                int partnerCount = (int) allRelations.stream()
                                .filter(r -> RelationStatus.ACCEPTED.getCode().equals(r.getStatus()))
                                .count();
                stats.setSupervisedCount(partnerCount); // 兼容旧字段
                stats.setSupervisorCount(partnerCount); // 兼容旧字段

                // 今日任务统计
                LocalDate today = LocalDate.now();
                LocalDateTime todayStart = today.atStartOfDay();
                LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

                List<CheckInLog> todayLogs = logService.list(
                                new LambdaQueryWrapper<CheckInLog>()
                                                .eq(CheckInLog::getUserId, userId)
                                                .ge(CheckInLog::getCheckTime, todayStart)
                                                .lt(CheckInLog::getCheckTime, todayEnd));

                // 获取用户任务总数（简化：取打卡记录数+未完成数）
                int todayCompleted = (int) todayLogs.stream()
                                .filter(log -> TaskStatus.NORMAL.getCode().equals(log.getStatus()) ||
                                                TaskStatus.LATE.getCode().equals(log.getStatus()))
                                .count();

                stats.setTodayCompletedCount(todayCompleted);
                stats.setTodayMissedCount(0); // 需要更复杂的逻辑计算
                stats.setTodayTaskCount(todayCompleted); // 简化
                stats.setTodayCompletionRate(todayCompleted > 0 ? 100.0 : 0.0);

                // 本周统计
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                List<CheckInLog> weekLogs = logService.list(
                                new LambdaQueryWrapper<CheckInLog>()
                                                .eq(CheckInLog::getUserId, userId)
                                                .ge(CheckInLog::getCheckTime, weekStart.atStartOfDay())
                                                .lt(CheckInLog::getCheckTime, todayEnd));
                stats.setWeekCompletedCount(weekLogs.size());
                stats.setWeekMissedCount(0);

                // 本月统计
                LocalDate monthStart = today.withDayOfMonth(1);
                List<CheckInLog> monthLogs = logService.list(
                                new LambdaQueryWrapper<CheckInLog>()
                                                .eq(CheckInLog::getUserId, userId)
                                                .ge(CheckInLog::getCheckTime, monthStart.atStartOfDay())
                                                .lt(CheckInLog::getCheckTime, todayEnd));
                stats.setMonthCompletedCount(monthLogs.size());
                stats.setMonthMissedCount(0);

                // 总打卡次数
                int totalCount = (int) logService.count(
                                new LambdaQueryWrapper<CheckInLog>()
                                                .eq(CheckInLog::getUserId, userId));
                stats.setTotalCheckInCount(totalCount);

                // 连续打卡天数（简化计算）
                stats.setStreakDays(calculateStreakDays(userId));

                // 近7天数据（用于图表）
                int[] weeklyTasks = new int[7];
                int[] weeklyCompleted = new int[7];
                String[] weeklyLabels = new String[7];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

                for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        weeklyLabels[6 - i] = date.format(formatter);

                        int completed = (int) logService.count(
                                        new LambdaQueryWrapper<CheckInLog>()
                                                        .eq(CheckInLog::getUserId, userId)
                                                        .ge(CheckInLog::getCheckTime, date.atStartOfDay())
                                                        .lt(CheckInLog::getCheckTime, date.plusDays(1).atStartOfDay()));
                        weeklyCompleted[6 - i] = completed;
                        weeklyTasks[6 - i] = completed; // 简化
                }

                stats.setWeeklyTaskCounts(weeklyTasks);
                stats.setWeeklyCompletedCounts(weeklyCompleted);
                stats.setWeeklyLabels(weeklyLabels);

                return stats;
        }

        /**
         * 计算连续打卡天数
         */
        private int calculateStreakDays(Long userId) {
                int streak = 0;
                LocalDate checkDate = LocalDate.now();

                while (true) {
                        long count = logService.count(
                                        new LambdaQueryWrapper<CheckInLog>()
                                                        .eq(CheckInLog::getUserId, userId)
                                                        .ge(CheckInLog::getCheckTime, checkDate.atStartOfDay())
                                                        .lt(CheckInLog::getCheckTime,
                                                                        checkDate.plusDays(1).atStartOfDay()));

                        if (count > 0) {
                                streak++;
                                checkDate = checkDate.minusDays(1);
                        } else {
                                break;
                        }

                        // 最多检查365天
                        if (streak > 365)
                                break;
                }

                return streak;
        }
}
