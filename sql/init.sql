/*
 Navicat Premium Data Transfer
 Target Server Type    : MySQL
 Target Server Version : 80000
 File Encoding         : 65001

 Date: 14/01/2026 14:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '微信OpenID',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for check_in_log
-- ----------------------------
DROP TABLE IF EXISTS `check_in_log`;
CREATE TABLE `check_in_log`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `task_id` bigint(0) DEFAULT NULL COMMENT '关联的任务ID',
  `check_time` datetime(0) NOT NULL COMMENT '打卡时间',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 1-正常, 2-补打卡, 3-未打卡',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '打卡日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_relation
-- ----------------------------
DROP TABLE IF EXISTS `user_relation`;
CREATE TABLE `user_relation`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `supervised_id` bigint(0) DEFAULT NULL COMMENT '被监督者ID',
  `supervisor_id` bigint(0) DEFAULT NULL COMMENT '监督者ID',
  `relation_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关系名称',
  `relation_type` tinyint(4) DEFAULT 0 COMMENT '关系类型: 0-自定义, 1-情侣, 2-挚友, 3-家人',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-待确认, 1-已接受, 2-已拒绝',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_relation`(`supervised_id`, `supervisor_id`) USING BTREE, 
  INDEX `idx_supervisor_id`(`supervisor_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for check_in_task
-- ----------------------------
DROP TABLE IF EXISTS `check_in_task`;
CREATE TABLE `check_in_task` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建者ID',
  `user_id` bigint(20) NOT NULL COMMENT '目标用户ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务标题',
  `remind_time` datetime DEFAULT NULL COMMENT '提醒时间',
  `repeat_type` tinyint(4) DEFAULT 1 COMMENT '重复类型: 0-单次, 1-每天, 2-工作日, 3-周末',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint(0) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人名称',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_general_ci COMMENT='叮咚任务表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
