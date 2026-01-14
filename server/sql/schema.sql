CREATE DATABASE IF NOT EXISTS `dingdong` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `dingdong`;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `role` varchar(20) DEFAULT NULL COMMENT '角色(ELDER/CHILD)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `check_in_log`;
CREATE TABLE `check_in_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `check_in_date` date NOT NULL COMMENT '打卡日期',
  `status` varchar(20) NOT NULL COMMENT '状态(COMPLETED/MISSED)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';
