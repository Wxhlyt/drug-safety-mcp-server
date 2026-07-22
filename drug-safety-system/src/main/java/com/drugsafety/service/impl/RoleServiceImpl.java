package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.Role;
import com.drugsafety.mapper.RoleMapper;
import com.drugsafety.service.RoleService;
import org.springframework.stereotype.Service;

/**
 * 角色业务实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}