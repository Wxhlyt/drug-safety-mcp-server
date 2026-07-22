-- ========================================================
-- 智能药品安全管理系统 中文测试数据初始化脚本
-- 包含：药品信息、风险记录、不良反应记录
-- 字符集：utf8mb4
-- ========================================================

USE drug_safety;
SET NAMES utf8mb4;

-- 清空旧数据
TRUNCATE TABLE adverse_reaction_record;
TRUNCATE TABLE drug_risk_record;
TRUNCATE TABLE drug_info;

-- 药品基础信息（5条中文数据）
INSERT INTO drug_info (drug_name, generic_name, drug_category, category_code, dosage_form, specification, storage_condition, manufacturer, approval_number, drug_description, risk_level, risk_score, status) VALUES
('阿莫西林胶囊', '阿莫西林', '抗感染药', 'ANTIINFECTIVE', '胶囊剂', '0.25g', '密封，阴凉干燥处保存', '华北制药', '国药准字H13020724', '广谱青霉素类抗生素，适用于敏感菌引起的呼吸道感染、尿路感染等', 'MEDIUM', 55.50, 1),
('布洛芬缓释胶囊', '布洛芬', '解热镇痛药', 'ANALGESIC', '胶囊剂', '0.3g', '密封保存', '中美史克', '国药准字H10900089', '非甾体抗炎药，用于缓解轻至中度疼痛及感冒引起的发热', 'LOW', 25.00, 1),
('头孢克肟片', '头孢克肟', '抗感染药', 'ANTIINFECTIVE', '片剂', '0.1g', '遮光，密封保存', '白云山制药', '国药准字H20040726', '第三代口服头孢菌素，用于敏感菌所致的呼吸系统、泌尿系统感染', 'MEDIUM', 60.00, 1),
('氯雷他定片', '氯雷他定', '抗过敏药', 'ANTIALLERGIC', '片剂', '10mg', '遮光，密封保存', '拜耳医药', '国药准字H20050041', '第二代抗组胺药，用于缓解过敏性鼻炎、慢性荨麻疹等症状', 'LOW', 20.00, 1),
('二甲双胍片', '二甲双胍', '降糖药', 'ANTIDIABETIC', '片剂', '0.5g', '密封保存', '施贵宝制药', '国药准字H20023370', '双胍类口服降糖药，用于2型糖尿病患者的血糖控制', 'HIGH', 75.00, 1);

-- 药品风险记录（5条中文数据）
INSERT INTO drug_risk_record (drug_id, risk_level, risk_score, risk_reason, analysis_result, analysis_type) VALUES
((SELECT id FROM drug_info WHERE drug_name = '阿莫西林胶囊'), 'MEDIUM', 55.50, '可能引起过敏反应，青霉素过敏者禁用', '该药品存在中等风险，需关注患者过敏史，用药前进行皮试', 'RULE'),
((SELECT id FROM drug_info WHERE drug_name = '布洛芬缓释胶囊'), 'LOW', 25.00, '常见胃肠道不适，长期大量使用可能影响肾功能', '风险较低，建议饭后服用，避免长期大剂量使用', 'RULE'),
((SELECT id FROM drug_info WHERE drug_name = '头孢克肟片'), 'MEDIUM', 60.00, '与其他药物可能存在相互作用，饮酒可能引发双硫仑样反应', '建议关注联合用药情况，用药期间避免饮酒', 'RULE'),
((SELECT id FROM drug_info WHERE drug_name = '氯雷他定片'), 'LOW', 20.00, '偶见嗜睡、乏力等神经系统反应', '风险较低，驾驶或操作机械前慎用', 'RULE'),
((SELECT id FROM drug_info WHERE drug_name = '二甲双胍片'), 'HIGH', 75.00, '可能导致乳酸酸中毒，肝肾功能不全者慎用', '高风险药品，需严格监测肝肾功能及血糖变化', 'RULE');

-- 不良反应记录（5条中文数据）
INSERT INTO adverse_reaction_record (drug_id, reaction_name, reaction_description, severity_level, occurrence_time, reporter, treatment, status) VALUES
((SELECT id FROM drug_info WHERE drug_name = '阿莫西林胶囊'), '皮疹', '服药后出现皮肤红疹，伴有轻度瘙痒', 'MEDIUM', '2026-07-20 10:00:00', '张医生', '停药并予抗过敏治疗，症状缓解', 1),
((SELECT id FROM drug_info WHERE drug_name = '布洛芬缓释胶囊'), '胃痛', '空腹服用后胃部不适，恶心感明显', 'LOW', '2026-07-19 14:30:00', '李医生', '建议改为饭后服用，必要时使用胃黏膜保护剂', 1),
((SELECT id FROM drug_info WHERE drug_name = '头孢克肟片'), '腹泻', '用药后出现轻度腹泻，每日2-3次', 'LOW', '2026-07-18 09:15:00', '王医生', '补充电解质，观察症状变化，必要时调整用药', 1),
((SELECT id FROM drug_info WHERE drug_name = '氯雷他定片'), '头晕', '服药后头晕乏力，嗜睡明显', 'MEDIUM', '2026-07-17 16:45:00', '赵医生', '建议睡前服用，避免驾驶或高空作业', 1),
((SELECT id FROM drug_info WHERE drug_name = '二甲双胍片'), '恶心', '用药初期出现恶心、食欲减退', 'LOW', '2026-07-16 11:20:00', '刘医生', '建议少量多餐，从小剂量开始逐渐加量', 1);
