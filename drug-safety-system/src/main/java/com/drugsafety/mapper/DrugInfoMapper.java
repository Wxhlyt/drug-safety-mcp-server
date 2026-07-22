package com.drugsafety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drugsafety.entity.DrugInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品基础信息 Mapper 接口
 */
@Mapper
public interface DrugInfoMapper extends BaseMapper<DrugInfo> {
}