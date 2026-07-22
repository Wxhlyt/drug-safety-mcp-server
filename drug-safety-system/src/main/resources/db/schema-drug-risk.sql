-- 药品风险等级分析模块数据库脚本
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS drug_safety DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drug_safety;

-- 药品风险评估记录表
CREATE TABLE IF NOT EXISTS `drug_risk_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '风险记录ID',
    `drug_id` BIGINT NOT NULL COMMENT '药品ID，关联 drug_info.id',
    `risk_level` VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' COMMENT '风险等级：LOW低/MEDIUM中/HIGH高/UNKNOWN未知',
    `risk_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '风险评分，范围 0.00-100.00',
    `risk_reason` TEXT COMMENT '风险原因说明',
    `analysis_result` TEXT COMMENT '风险分析结果',
    `analysis_type` VARCHAR(20) NOT NULL DEFAULT 'RULE' COMMENT '分析类型：RULE规则分析 / AI辅助分析',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_drug_id` (`drug_id`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_drug_risk_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `drug_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品风险评估记录表';