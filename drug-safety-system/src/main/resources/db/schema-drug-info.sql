-- 智能药品安全管理系统 - 药品基础信息表
-- 数据库：drug_safety
-- 字符集：utf8mb4
-- 创建时间：2026-07-19

CREATE DATABASE IF NOT EXISTS drug_safety DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drug_safety;

DROP TABLE IF EXISTS `drug_info`;

CREATE TABLE `drug_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '药品ID，主键',
    `drug_name` VARCHAR(200) NOT NULL COMMENT '药品名称（商品名）',
    `generic_name` VARCHAR(200) NOT NULL COMMENT '通用名称',
    `drug_category` VARCHAR(100) DEFAULT NULL COMMENT '药品分类：如抗生素、降压药、镇痛药等',
    `category_code` VARCHAR(50) DEFAULT NULL COMMENT '药品分类编码，用于分类统计和系统内部标识，如 ANTIBIOTIC、ANTIHYPERTENSIVE',
    `dosage_form` VARCHAR(100) DEFAULT NULL COMMENT '剂型，例如片剂、胶囊、注射液',
    `specification` VARCHAR(200) DEFAULT NULL COMMENT '药品规格，例如 500mg/片、10ml:0.1g',
    `storage_condition` VARCHAR(200) DEFAULT NULL COMMENT '储存条件，例如常温、2-8℃冷藏、避光保存',
    `manufacturer` VARCHAR(200) DEFAULT NULL COMMENT '生产厂家',
    `approval_number` VARCHAR(100) DEFAULT NULL COMMENT '批准文号，如国药准字H20240001',
    `drug_description` TEXT DEFAULT NULL COMMENT '药品描述、适应症、用法用量等',
    `risk_level` VARCHAR(20) DEFAULT 'LOW' COMMENT '风险等级：LOW(低)/MEDIUM(中)/HIGH(高)/UNKNOWN(未知)',
    `risk_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '风险评分，0.00-100.00，用于AI风险分析排序',
    `adverse_reactions` TEXT DEFAULT NULL COMMENT '已知不良反应记录，JSON或文本格式',
    `ai_risk_analysis` TEXT DEFAULT NULL COMMENT 'AI风险分析结果摘要',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_approval_number` (`approval_number`),
    KEY `idx_drug_name` (`drug_name`),
    KEY `idx_generic_name` (`generic_name`),
    KEY `idx_drug_category` (`drug_category`),
    KEY `idx_category_code` (`category_code`),
    KEY `idx_dosage_form` (`dosage_form`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品基础信息表';
