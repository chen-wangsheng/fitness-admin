-- Flyway V2: 补齐高频查询索引(2026-06-13)
-- 幂等:用存储过程判断索引是否存在,存在则跳过,避免重复创建和 DROP 触发外键约束
-- 来源: sql/2026-06-13_add_indexes.sql
-- 说明: 在 dev/test 环境会自动 apply,prod 环境先通过 FLYWAY_ENABLED=true 启动

-- 通用工具:如果索引不存在则创建
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

-- 1. community 帖子
CALL add_index_if_not_exists('post', 'idx_status_created', '`status`, `created_at`');

-- 2. community 评论
CALL add_index_if_not_exists('comment', 'idx_user', '`user_id`');
CALL add_index_if_not_exists('comment', 'idx_parent', '`parent_id`');
CALL add_index_if_not_exists('comment', 'idx_created', '`created_at`');

-- 3. user
CALL add_index_if_not_exists('user', 'idx_status', '`status`');
CALL add_index_if_not_exists('user', 'idx_created_at', '`created_at`');

-- 4. ai_safety_event
CALL add_index_if_not_exists('ai_safety_event', 'idx_user', '`user_id`');
CALL add_index_if_not_exists('ai_safety_event', 'idx_session', '`session_id`');
CALL add_index_if_not_exists('ai_safety_event', 'idx_created', '`created_at`');

-- 5. announcement
CALL add_index_if_not_exists('announcement', 'idx_status', '`status`');
CALL add_index_if_not_exists('announcement', 'idx_publish_at', '`publish_time`');

-- 6. ai_chat_session
CALL add_index_if_not_exists('ai_chat_session', 'idx_user_status_updated', '`user_id`, `status`, `updated_at`');

-- 7. weekly_training_summary
CALL add_index_if_not_exists('weekly_training_summary', 'idx_user_week_start', '`user_id`, `week_start_date`');

-- 8. plan_load_adjustment
CALL add_index_if_not_exists('plan_load_adjustment', 'idx_plan_created', '`ai_plan_id`, `created_at`');

-- 9. report
CALL add_index_if_not_exists('report', 'idx_status_created', '`status`, `created_at`');

DROP PROCEDURE add_index_if_not_exists;
