-- ========================================================
-- 智能药品安全管理系统 风险分析记录表字段增量脚本
-- 作用：为 ai_risk_analysis_record 表新增风险等级与风险评分字段，
--       用于持久化每次风险分析计算出的等级与评分。
-- 注意：本脚本为增量修改（ALTER TABLE），不会 DROP TABLE，也不会清空现有数据。
-- 执行前提：ai_risk_analysis_record 表已存在（由 schema-ai-risk.sql 创建）。
-- ========================================================

USE drug_safety;

-- 风险等级：HIGH(高) / MEDIUM(中) / LOW(低)
ALTER TABLE `ai_risk_analysis_record`
    ADD COLUMN `risk_level` VARCHAR(20) DEFAULT NULL COMMENT '风险等级：HIGH高/MEDIUM中/LOW低' AFTER `drug_id`;

-- 风险评分：范围 0.00-100.00，对应最终计算分数
ALTER TABLE `ai_risk_analysis_record`
    ADD COLUMN `risk_score` DECIMAL(5,2) DEFAULT NULL COMMENT '风险评分，范围 0.00-100.00' AFTER `risk_level`;