-- Flyway V3: AI 对话消息支持流式分片(2026-06-13)
-- 幂等:用存储过程判断列/索引是否存在,只在缺失时添加
-- 来源: P1-3.3 AI 流式响应增加超时 & 断点续传
-- 说明: 为 ai_chat_message 增加 chunk_index / chunk_total / is_stream / stream_status 字段

-- 通用工具:列不存在则添加
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_col VARCHAR(64),
    IN p_def TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_col, '` ', p_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- 通用工具:索引不存在则创建
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

-- 1. 加列(逐列,失败单点不影响其他)
CALL add_column_if_not_exists('ai_chat_message', 'chunk_index',
    'SMALLINT UNSIGNED DEFAULT NULL COMMENT "流式分片序号" AFTER `feedback`');
CALL add_column_if_not_exists('ai_chat_message', 'chunk_total',
    'SMALLINT UNSIGNED DEFAULT NULL COMMENT "总分片数" AFTER `chunk_index`');
CALL add_column_if_not_exists('ai_chat_message', 'is_stream',
    'TINYINT(1) NOT NULL DEFAULT 0 COMMENT "是否流式消息" AFTER `chunk_total`');
CALL add_column_if_not_exists('ai_chat_message', 'stream_status',
    'TINYINT(1) NOT NULL DEFAULT 1 COMMENT "流式状态: 1-生成中 2-已完成 3-失败" AFTER `is_stream`');

-- 2. 加索引
CALL add_index_if_not_exists('ai_chat_message', 'idx_session_created', '`session_id`, `created_at`');
CALL add_index_if_not_exists('ai_chat_message', 'idx_stream_status', '`is_stream`, `stream_status`');

DROP PROCEDURE add_column_if_not_exists;
DROP PROCEDURE add_index_if_not_exists;
