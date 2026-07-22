package com.drugsafety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drugsafety.entity.Role;
import com.drugsafety.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 分配用户角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
}