-- Flyway V4: 社区增强 - 置顶字段 + 用户禁言表
-- 日期: 2026-06-14
-- 来源: sql/00_full_schema.sql 的 8.1 / 8.6 章节
-- 影响: post 表加 pinned/pinned_at 字段和索引;新建 user_mute 表
-- 幂等: 用存储过程判断列/表是否存在,存在则跳过
-- 说明: dev/test 环境会自动 apply;prod 启动时 FLYWAY_ENABLED=true

-- 工具:如果列不存在则添加
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_def TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
        LIMIT 1
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- 工具:如果索引不存在则创建
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
CREATE PROCEDURE add_index_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_cols VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND INDEX_NAME = p_index
        LIMIT 1
    ) THEN
        SET @sql = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_cols, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- 1. post 表加置顶字段
CALL add_column_if_not_exists('post', 'pinned', "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是' AFTER `status`");
CALL add_column_if_not_exists('post', 'pinned_at', "DATETIME DEFAULT NULL COMMENT '置顶时间' AFTER `pinned`");
CALL add_index_if_not_exists('post', 'idx_pinned_created', '`pinned`, `created_at`');

-- 2. 新建 user_mute 表(IF NOT EXISTS 由 Flyway 历史表保证)
CREATE TABLE IF NOT EXISTS `user_mute` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '被禁言用户ID(关联 user.id)',
  `reason` VARCHAR(256) NOT NULL COMMENT '禁言原因',
  `duration_days` INT NOT NULL COMMENT '禁言时长(天),-1 表示永久',
  `start_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '禁言开始时间',
  `end_at` DATETIME DEFAULT NULL COMMENT '禁言结束时间,NULL 表示永久',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作管理员ID(关联 admin_user.id)',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1-生效中 0-已解除 2-已过期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_end` (`status`, `end_at`),
  CONSTRAINT `fk_um_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户禁言记录';

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
