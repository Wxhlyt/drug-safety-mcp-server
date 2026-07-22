package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.Role;
import com.drugsafety.entity.User;
import com.drugsafety.entity.UserRole;
import com.drugsafety.mapper.UserMapper;
import com.drugsafety.mapper.UserRoleMapper;
import com.drugsafety.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return userRoleMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        System.out.println("===== UserServiceImpl.assignRoles =====");
        System.out.println("userId=" + userId);
        System.out.println("roleIds=" + roleIds);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        int deleteCount = userRoleMapper.delete(wrapper);
        System.out.println("删除旧角色关联数量=" + deleteCount);

        if (!CollectionUtils.isEmpty(roleIds)) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                int insertCount = userRoleMapper.insert(userRole);
                System.out.println("插入用户角色关联: userId=" + userId + ", roleId=" + roleId + ", 结果=" + insertCount);
            }
        }
        System.out.println("===== UserServiceImpl.assignRoles 完成 =====");
    }
}