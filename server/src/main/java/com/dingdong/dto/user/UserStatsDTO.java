package com.dingdong.dto.user;

import lombok.Data;

/**
 * 用户统计数据DTO
 */
@Data
public class UserStatsDTO {
    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String nickname;

    /** 等级名称 */
    private String levelName;

    // ====== 监督统计 ======
    /** 我监督的人数 */
    private Integer supervisedCount;

    /** 监督我的人数 */
    private Integer supervisorCount;

    /** 最大可监督人数 */
    private Integer maxSupervisedCount;

    // ====== 任务统计 ======
    /** 今日任务总数 */
    private Integer todayTaskCount;

    /** 今日已完成任务数 */
    private Integer todayCompletedCount;

    /** 今日错过任务数 */
    private Integer todayMissedCount;

    /** 今日完成率 */
    private Double todayCompletionRate;

    // ====== 历史统计 ======
    /** 本周完成任务数 */
    private Integer weekCompletedCount;

    /** 本周错过任务数 */
    private Integer weekMissedCount;

    /** 本月完成任务数 */
    private Integer monthCompletedCount;

    /** 本月错过任务数 */
    private Integer monthMissedCount;

    /** 连续打卡天数 */
    private Integer streakDays;

    /** 总打卡次数 */
    private Integer totalCheckInCount;

    // ====== 周统计（用于图表） ======
    /** 近7天每天任务数 [周一, 周二, ..., 周日] */
    private int[] weeklyTaskCounts;

    /** 近7天每天完成数 */
    private int[] weeklyCompletedCounts;

    /** 近7天日期标签 */
    private String[] weeklyLabels;
}
