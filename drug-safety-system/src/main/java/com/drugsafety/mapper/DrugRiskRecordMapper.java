package com.drugsafety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drugsafety.entity.DrugRiskRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品风险评估记录 Mapper 接口
 */
@Mapper
public interface DrugRiskRecordMapper extends BaseMapper<DrugRiskRecord> {
}