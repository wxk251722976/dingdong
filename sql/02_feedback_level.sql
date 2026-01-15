-- 用户反馈表（包含BaseEntity字段）
DROP TABLE IF EXISTS `user_feedback`;
CREATE TABLE `user_feedback` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-已处理',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户反馈表' ROW_FORMAT = Dynamic;

-- 用户等级配置表（包含BaseEntity字段）
DROP TABLE IF EXISTS `user_level_config`;
CREATE TABLE `user_level_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `level_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级编码',
  `level_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级名称',
  `max_supervised_count` int NOT NULL DEFAULT 3 COMMENT '最大监督人数',
  `max_task_per_user` int NOT NULL DEFAULT 2 COMMENT '每人最大任务数',
  `max_feedback_per_day` int NOT NULL DEFAULT 3 COMMENT '每日最大反馈数',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level_code`(`level_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户等级配置表' ROW_FORMAT = Dynamic;

-- 初始化默认等级配置
INSERT INTO `user_level_config` (`id`, `level_code`, `level_name`, `max_supervised_count`, `max_task_per_user`, `max_feedback_per_day`, `description`, `sort_order`, `create_time`, `deleted`) VALUES
(1, 'NORMAL', '普通用户', 3, 2, 3, '默认等级，可监督3人，每人2个任务', 1, NOW(), 0),
(2, 'VIP', 'VIP用户', 10, 5, 10, 'VIP等级，可监督10人，每人5个任务', 2, NOW(), 0),
(3, 'SVIP', 'SVIP用户', 50, 20, 50, 'SVIP等级，可监督50人，每人20个任务', 3, NOW(), 0);

-- 用户表增加等级字段
ALTER TABLE `sys_user` ADD COLUMN `level_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NORMAL' COMMENT '用户等级编码';
