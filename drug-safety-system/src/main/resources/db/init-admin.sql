-- ========================================================
-- 智能药品安全管理系统 初始化管理员账号
-- 密码使用 BCrypt 加密，不要明文保存
-- ========================================================

USE drug_safety;

-- 初始化管理员账号
INSERT INTO sys_user (username, password, real_name, phone, email, status)
VALUES ('wxhlyt', '$2a$10$bwcBmnuP999iPzaOkmGzduT8H96B2rU5s0SmbM.0gCN00CDAKABoa', 'System Admin', '13800138001', 'wxhlyt@drugsafety.com', 1)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    real_name = VALUES(real_name),
    phone = VALUES(phone),
    email = VALUES(email),
    status = VALUES(status);