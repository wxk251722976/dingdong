/**
 * 前端常量定义
 * 与后端枚举保持一致
 */

/**
 * 任务完成状态 - 与后端 TaskStatus 枚举对应
 */
export const TaskStatus = {
    PENDING: { code: 0, desc: '待完成' },
    NORMAL: { code: 1, desc: '已完成' },
    LATE: { code: 2, desc: '补打卡' },
    MISSED: { code: 3, desc: '已错过' }
};

/**
 * 关系确认状态 - 与后端 RelationStatus 枚举对应
 */
export const RelationStatus = {
    PENDING: { code: 0, desc: '待确认' },
    CONFIRMED: { code: 1, desc: '已确认' }
};

/**
 * 任务启用状态 - 与后端 TaskEnabled 枚举对应
 */
export const TaskEnabled = {
    DISABLED: { code: 0, desc: '停用' },
    ENABLED: { code: 1, desc: '启用' }
};

/**
 * 用户角色 - 与后端 UserRole 枚举对应
 */
export const UserRole = {
    ELDER: { code: 'ELDER', desc: '长辈' },
    CHILD: { code: 'CHILD', desc: '子女' }
};

/**
 * 重复类型 - 与后端 RepeatType 枚举对应
 */
export const RepeatType = {
    ONCE: { code: 0, desc: '单次' },
    DAILY: { code: 1, desc: '每天' },
    WEEKDAYS: { code: 2, desc: '工作日' },
    WEEKENDS: { code: 3, desc: '周末' }
};
