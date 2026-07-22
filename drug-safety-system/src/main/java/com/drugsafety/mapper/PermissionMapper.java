package com.drugsafety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drugsafety.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper（预留扩展结构）
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}