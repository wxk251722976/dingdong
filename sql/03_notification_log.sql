/*
 通知推送记录表
 用于记录所有通知推送，防止重复推送
 Date: 18/01/2026
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for notification_log
-- ----------------------------
DROP TABLE IF EXISTS `notification_log`;
CREATE TABLE `notification_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `notify_date` date NOT NULL COMMENT '通知日期',
  `notify_type` tinyint(4) NOT NULL COMMENT '通知类型: 1-叮咚提醒, 2-打卡完成, 3-漏打卡, 4-补打卡',
  `user_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-成功, 0-失败',
  `error_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_task_date_type` (`task_id`, `notify_date`, `notify_type`) USING BTREE COMMENT '防止重复推送',
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知推送记录表' ROW_FORMAT=Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
