-- AI风险分析模块数据库脚本
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS drug_safety DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drug_safety;

-- AI风险分析记录表
CREATE TABLE IF NOT EXISTS `ai_risk_analysis_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI风险分析记录ID',
    `drug_id` BIGINT NOT NULL COMMENT '药品ID，关联 drug_info.id',
    `analysis_content` TEXT COMMENT 'AI分析内容',
    `risk_suggestion` TEXT COMMENT '风险建议',
    `analysis_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'AI分析时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_drug_id` (`drug_id`),
    KEY `idx_analysis_time` (`analysis_time`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_ai_risk_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `drug_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI风险分析记录表';
