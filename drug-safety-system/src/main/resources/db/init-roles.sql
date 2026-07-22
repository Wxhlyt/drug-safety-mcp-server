-- ========================================================
-- 智能药品安全管理系统 初始化角色数据
-- 定义系统基础角色并绑定管理员账号
-- ========================================================

USE drug_safety;

-- 初始化系统角色
INSERT INTO sys_role (role_name, role_code, description, status)
VALUES ('管理员', 'ADMIN', '系统管理员，拥有所有菜单和操作权限', 1),
       ('普通用户', 'USER', '普通用户，可查看和操作基础业务数据', 1)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    description = VALUES(description),
    status = VALUES(status);

-- 将管理员角色绑定到 wxhlyt 账号
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u,
     sys_role r
WHERE u.username = 'wxhlyt'
  AND r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id),
                        role_id = VALUES(role_id);
