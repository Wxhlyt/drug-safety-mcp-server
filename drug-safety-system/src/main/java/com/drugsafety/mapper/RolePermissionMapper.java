package com.drugsafety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drugsafety.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper（预留扩展结构）
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}