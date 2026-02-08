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

import lombok.Data;

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
     * 统计用户指定日期范围内的打卡天数 (优化版: 批量获取)
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 打卡天数
     */
    public int countCheckInsInRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 如果跨年，需分段处理（目前设计Key按年存储）
        if (startDate.getYear() != endDate.getYear()) {
            LocalDate endOfFirstYear = LocalDate.of(startDate.getYear(), 12, 31);
            LocalDate startOfSecondYear = LocalDate.of(endDate.getYear(), 1, 1);
            return countCheckInsInRange(userId, startDate, endOfFirstYear) +
                    countCheckInsInRange(userId, startOfSecondYear, endDate);
        }

        String key = getUserKey(userId, startDate.getYear());
        byte[] bytes = redisUtil.get(key.getBytes());

        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        int startDay = startDate.getDayOfYear() - 1;
        int endDay = endDate.getDayOfYear() - 1;
        int count = 0;

        for (int i = startDay; i <= endDay; i++) {
            if (getBit(bytes, i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 辅助方法：无需 Redis 操作，直接从 byte数组获取 bit
     */
    private boolean getBit(byte[] bytes, int offset) {
        int byteIndex = offset / 8;
        int bitIndex = 7 - (offset % 8); // Redis bit order is big-endian within byte

        if (byteIndex >= bytes.length) {
            return false;
        }

        return (bytes[byteIndex] & (1 << bitIndex)) != 0;
    }

    /**
     * 计算用户连续打卡天数（从今天往前算） (优化版: 支持跨年)
     *
     * @param userId 用户ID
     * @return 连续打卡天数
     */
    public int calculateStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        String key = getUserKey(userId, currentYear);
        byte[] bytes = redisUtil.get(key.getBytes());
        byte[] prevBytes = null; // 懒加载去年数据

        int streak = 0;

        // 最多检查365天
        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            int year = checkDate.getYear();
            int dayOfYear = checkDate.getDayOfYear() - 1;

            boolean isCheckedIn = false;

            if (year == currentYear) {
                if (bytes != null) {
                    isCheckedIn = getBit(bytes, dayOfYear);
                }
            } else {
                // 跨年了，加载去年数据
                if (prevBytes == null) {
                    String prevKey = getUserKey(userId, year);
                    prevBytes = redisUtil.get(prevKey.getBytes());
                }
                if (prevBytes != null) {
                    isCheckedIn = getBit(prevBytes, dayOfYear);
                }
            }

            if (isCheckedIn) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算用户最长连续打卡天数（指定年份） (优化版: 内存位运算)
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 最长连续打卡天数
     */
    public int calculateMaxStreakDays(Long userId, int year) {
        String key = getUserKey(userId, year);
        byte[] bytes = redisUtil.get(key.getBytes());

        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        int maxStreak = 0;
        int currentStreak = 0;
        // 366天覆盖闰年
        int daysInYear = LocalDate.of(year, 12, 31).getDayOfYear();

        for (int i = 0; i < daysInYear; i++) {
            if (getBit(bytes, i)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return maxStreak;
    }

    /**
     * 获取用户指定日期范围内的打卡记录 (优化版: 批量获取)
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期 -> 是否打卡 的映射
     */
    public Map<String, Boolean> getCheckInRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Boolean> records = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 简单处理：如果范围跨年，逻辑会比较复杂，这里暂简化为循环当年
        // 生产环境应根据每一天所属年份获取对应的 bytes

        // 优化策略：按年分组处理
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int year = current.getYear();
            LocalDate endOfYear = LocalDate.of(year, 12, 31);
            if (endOfYear.isAfter(endDate)) {
                endOfYear = endDate;
            }

            String key = getUserKey(userId, year);
            byte[] bytes = redisUtil.get(key.getBytes());

            while (!current.isAfter(endOfYear)) {
                boolean isCheckedIn = false;
                if (bytes != null) {
                    isCheckedIn = getBit(bytes, current.getDayOfYear() - 1);
                }
                records.put(current.format(formatter), isCheckedIn);
                current = current.plusDays(1);
            }
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
     * 获取用户近 N 天的每日打卡次数（简化版，只统计是否打卡） (优化版: 批量获取)
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 每天的打卡情况列表
     */
    public List<DailyCheckInStat> getRecentDailyStats(Long userId, int days) {
        List<DailyCheckInStat> stats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        // 批量获取打卡记录（已处理跨年逻辑）
        Map<String, Boolean> records = getCheckInRecords(userId, startDate, today);

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String key = date.format(keyFormatter);
            Boolean isCheckedIn = records.getOrDefault(key, false);

            DailyCheckInStat stat = new DailyCheckInStat();
            stat.setDate(date.format(formatter));
            stat.setCheckedIn(isCheckedIn);
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
    @Data
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
