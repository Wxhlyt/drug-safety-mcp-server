-- 不良反应记录模块数据库脚本
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS drug_safety DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drug_safety;

-- 不良反应记录表
CREATE TABLE IF NOT EXISTS `adverse_reaction_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '不良反应记录ID',
    `drug_id` BIGINT NOT NULL COMMENT '药品ID，关联 drug_info.id',
    `reaction_name` VARCHAR(100) NOT NULL COMMENT '不良反应名称',
    `reaction_description` TEXT COMMENT '不良反应描述',
    `severity_level` VARCHAR(20) NOT NULL DEFAULT 'LOW' COMMENT '严重程度：LOW低/MEDIUM中/HIGH高',
    `occurrence_time` DATETIME COMMENT '不良反应发生时间',
    `reporter` VARCHAR(50) COMMENT '报告人',
    `treatment` TEXT COMMENT '处理措施',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_drug_id` (`drug_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品不良反应记录表';
