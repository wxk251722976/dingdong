package com.dingdong.service.checkin;

import com.dingdong.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡统计 Redis Bitmap 服务
 * 
 * 使用 Redis Bitmap 实现高效的打卡统计功能
 * 
 * Key 设计:
 * - 用户年度打卡记录: checkin:user:{userId}:{year}
 * - 任务年度打卡记录: checkin:task:{taskId}:{year}
 * - 用户任务打卡记录: checkin:user:{userId}:task:{taskId}:{year}
 * 
 * Bitmap 偏移量设计:
 * - 每个位表示一年中的第几天 (0-365)
 * - 位值 1 表示当天已打卡，0 表示未打卡
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInBitmapService {

    private final RedisUtil redisUtil;

    // Key 前缀
    private static final String KEY_PREFIX_USER = "checkin:user:";
    private static final String KEY_PREFIX_TASK = "checkin:task:";
    private static final String KEY_PREFIX_USER_TASK = "checkin:user:";

    /**
     * 记录用户打卡
     *
     * @param userId 用户ID
     * @param date   打卡日期
     */
    public void recordCheckIn(Long userId, LocalDate date) {
        String key = getUserKey(userId, date.getYear());
        int dayOfYear = date.getDayOfYear() - 1; // 0-based offset
        redisUtil.setBit(key, dayOfYear, true);
        log.debug("记录用户打卡: userId={}, date={}, key={}, offset={}", userId, date, key, dayOfYear);
    }

    /**
     * 记录用户在指定任务的打卡
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param date   打卡日期
     */
    public void recordTaskCheckIn(Long userId, Long taskId, LocalDate date) {
        // 记录用户总体打卡
        recordCheckIn(userId, date);

        // 记录任务打卡
        String taskKey = getTaskKey(taskId, date.getYear());
        int dayOfYear = date.getDayOfYear() - 1;
        redisUtil.setBit(taskKey, dayOfYear, true);

        // 记录用户在该任务的打卡
        String userTaskKey = getUserTaskKey(userId, taskId, date.getYear());
        redisUtil.setBit(userTaskKey, dayOfYear, true);

        log.debug("记录任务打卡: userId={}, taskId={}, date={}", userId, taskId, date);
    }

    /**
     * 检查用户某天是否已打卡
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 是否已打卡
     */
    public boolean hasCheckedIn(Long userId, LocalDate date) {
        String key = getUserKey(userId, date.getYear());
        int dayOfYear = date.getDayOfYear() - 1;
        Boolean result = redisUtil.getBit(key, dayOfYear);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 检查用户某天在指定任务是否已打卡
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param date   日期
     * @return 是否已打卡
     */
    public boolean hasTaskCheckedIn(Long userId, Long taskId, LocalDate date) {
        String key = getUserTaskKey(userId, taskId, date.getYear());
        int dayOfYear = date.getDayOfYear() - 1;
        Boolean result = redisUtil.getBit(key, dayOfYear);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 统计用户指定年份的总打卡天数
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 打卡天数
     */
    public int countYearlyCheckIns(Long userId, int year) {
        String key = getUserKey(userId, year);
        Long count = redisUtil.bitCount(key);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 统计用户指定月份的打卡天数
     *
     * @param userId 用户ID
     * @param year   年份
     * @param month  月份 (1-12)
     * @return 打卡天数
     */
    public int countMonthlyCheckIns(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return countCheckInsInRange(userId, startDate, endDate);
    }

    /**
     * 统计用户指定日期范围内的打卡天数
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 打卡天数
     */
    public int countCheckInsInRange(Long userId, LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (hasCheckedIn(userId, current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    /**
     * 计算用户连续打卡天数（从今天往前算）
     *
     * @param userId 用户ID
     * @return 连续打卡天数
     */
    public int calculateStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        // 最多检查365天
        for (int i = 0; i < 365; i++) {
            if (hasCheckedIn(userId, checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算用户最长连续打卡天数（指定年份）
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 最长连续打卡天数
     */
    public int calculateMaxStreakDays(Long userId, int year) {
        // key 变量预留用于后续 Redis BITFIELD 优化
        log.debug("计算最长连续打卡天数: userId={}, year={}, key={}", userId, year, getUserKey(userId, year));
        int maxStreak = 0;
        int currentStreak = 0;

        LocalDate date = LocalDate.of(year, 1, 1);
        LocalDate endDate = year == LocalDate.now().getYear()
                ? LocalDate.now()
                : LocalDate.of(year, 12, 31);

        while (!date.isAfter(endDate)) {
            if (hasCheckedIn(userId, date)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            date = date.plusDays(1);
        }

        return maxStreak;
    }

    /**
     * 获取用户指定日期范围内的打卡记录
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期 -> 是否打卡 的映射
     */
    public Map<String, Boolean> getCheckInRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Boolean> records = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            records.put(current.format(formatter), hasCheckedIn(userId, current));
            current = current.plusDays(1);
        }

        return records;
    }

    /**
     * 获取用户近 N 天的打卡记录
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 日期 -> 是否打卡 的映射
     */
    public Map<String, Boolean> getRecentCheckInRecords(Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return getCheckInRecords(userId, startDate, endDate);
    }

    /**
     * 获取用户近 N 天的每日打卡次数（简化版，只统计是否打卡）
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 每天的打卡情况列表
     */
    public List<DailyCheckInStat> getRecentDailyStats(Long userId, int days) {
        List<DailyCheckInStat> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            DailyCheckInStat stat = new DailyCheckInStat();
            stat.setDate(date.format(formatter));
            stat.setCheckedIn(hasCheckedIn(userId, date));
            stat.setCount(stat.isCheckedIn() ? 1 : 0);
            stats.add(stat);
        }

        return stats;
    }

    /**
     * 获取今日打卡统计
     *
     * @param userId 用户ID
     * @return 今日是否已打卡
     */
    public boolean hasTodayCheckedIn(Long userId) {
        return hasCheckedIn(userId, LocalDate.now());
    }

    /**
     * 获取本周打卡天数
     *
     * @param userId 用户ID
     * @return 本周打卡天数
     */
    public int countWeeklyCheckIns(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return countCheckInsInRange(userId, weekStart, today);
    }

    /**
     * 获取本月打卡天数
     *
     * @param userId 用户ID
     * @return 本月打卡天数
     */
    public int countCurrentMonthCheckIns(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        return countCheckInsInRange(userId, monthStart, today);
    }

    // ========== Key 生成方法 ==========

    private String getUserKey(Long userId, int year) {
        return KEY_PREFIX_USER + userId + ":" + year;
    }

    private String getTaskKey(Long taskId, int year) {
        return KEY_PREFIX_TASK + taskId + ":" + year;
    }

    private String getUserTaskKey(Long userId, Long taskId, int year) {
        return KEY_PREFIX_USER_TASK + userId + ":task:" + taskId + ":" + year;
    }

    // ========== 内部类 ==========

    /**
     * 每日打卡统计
     */
    @lombok.Data
    public static class DailyCheckInStat {
        /**
         * 日期（格式：MM-dd）
         */
        private String date;

        /**
         * 是否已打卡
         */
        private boolean checkedIn;

        /**
         * 打卡次数
         */
        private int count;
    }
}
