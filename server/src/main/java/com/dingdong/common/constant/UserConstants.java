package com.dingdong.common.constant;

/**
 * 用户相关常量
 */
public interface UserConstants {

    /** 默认等级编码 */
    String DEFAULT_LEVEL_CODE = "NORMAL";

    /** 默认等级名称 */
    String DEFAULT_LEVEL_NAME = "普通用户";

    /** 反馈状态：待处理 */
    int FEEDBACK_STATUS_PENDING = 0;

    /** 反馈状态：已处理 */
    int FEEDBACK_STATUS_RESOLVED = 1;

    // ====== 默认限制配置（兜底使用） ======

    /** 默认每人最大任务数 */
    int DEFAULT_MAX_TASK_PER_USER = 2;

    /** 默认每日最大反馈数 */
    int DEFAULT_MAX_FEEDBACK_PER_DAY = 3;

    /** 默认最大监督人数 */
    int DEFAULT_MAX_SUPERVISED_COUNT = 3;
}
