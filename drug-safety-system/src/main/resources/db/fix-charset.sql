-- ========================================================
-- 智能药品安全管理系统 数据库字符集修复脚本
-- 作用：将数据库及所有相关表统一修复为 utf8mb4 编码
-- 执行前请确认已备份重要数据
-- ========================================================

-- 修复数据库字符集
ALTER DATABASE drug_safety CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修复表字符集
ALTER TABLE drug_info CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE drug_risk_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE adverse_reaction_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ai_risk_analysis_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_user_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
