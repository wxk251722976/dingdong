-- ==========================================================
-- 用户关系表重构：平等化 + 历史记录分离
-- 执行顺序：建议在开发环境测试后再在生产环境执行
-- ==========================================================

-- 1. 新建关系历史记录表
DROP TABLE IF EXISTS `relation_history`;
CREATE TABLE `relation_history` (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `relation_id` bigint(0) NOT NULL COMMENT '关系ID',
  `action_type` tinyint(4) NOT NULL COMMENT '操作类型: 1-绑定, 2-发起解绑, 3-撤回解绑, 4-解绑完成',
  `operator_id` bigint(0) NOT NULL COMMENT '操作人ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作原因/备注',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relation_id`(`relation_id`) USING BTREE,
  INDEX `idx_operator_id`(`operator_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '关系历史记录表' ROW_FORMAT = Dynamic;

-- 2. 备份原 user_relation 表（可选，生产环境建议备份）
-- CREATE TABLE `user_relation_backup` AS SELECT * FROM `user_relation`;

-- 3. 修改 user_relation 表结构
-- 3.1 添加新字段（initiator_id: 发起人/邀请者, partner_id: 伙伴/被邀请者）
ALTER TABLE `user_relation` 
ADD COLUMN `initiator_id` bigint(0) DEFAULT NULL COMMENT '发起人ID（邀请者）' AFTER `id`,
ADD COLUMN `partner_id` bigint(0) DEFAULT NULL COMMENT '伙伴ID（被邀请者）' AFTER `initiator_id`;

-- 3.2 数据迁移：将原有数据迁移到新字段
-- supervisor_id 作为 initiator_id，supervised_id 作为 partner_id
UPDATE `user_relation` SET 
  `initiator_id` = `supervisor_id`,
  `partner_id` = `supervised_id`;

-- 3.3 设置新字段为非空（数据迁移后）
ALTER TABLE `user_relation` 
MODIFY COLUMN `initiator_id` bigint(0) NOT NULL COMMENT '发起人ID（邀请者）',
MODIFY COLUMN `partner_id` bigint(0) NOT NULL COMMENT '伙伴ID（被邀请者）';

-- 3.4 删除原有的唯一索引并创建新的
ALTER TABLE `user_relation` DROP INDEX `uk_relation`;
ALTER TABLE `user_relation` DROP INDEX `idx_supervisor_id`;
-- 注意：这里不再强制 initiator_id < partner_id，因为语义更清晰
ALTER TABLE `user_relation` 
ADD UNIQUE INDEX `uk_relation`(`initiator_id`, `partner_id`) USING BTREE,
ADD INDEX `idx_initiator`(`initiator_id`) USING BTREE,
ADD INDEX `idx_partner`(`partner_id`) USING BTREE;

-- 3.5 删除旧字段（确认数据迁移成功后执行）
ALTER TABLE `user_relation` 
DROP COLUMN `supervisor_id`,
DROP COLUMN `supervised_id`;

-- 3.7 修改状态字段注释（扩展状态值）
-- 状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中, 4-已解绑
ALTER TABLE `user_relation` 
MODIFY COLUMN `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中, 4-已解绑';

-- 3.8 添加解绑生效时间字段（用于24小时倒计时）
ALTER TABLE `user_relation` 
ADD COLUMN `unbind_expire_time` datetime(0) DEFAULT NULL COMMENT '解绑生效时间（24小时后）' AFTER `status`;
