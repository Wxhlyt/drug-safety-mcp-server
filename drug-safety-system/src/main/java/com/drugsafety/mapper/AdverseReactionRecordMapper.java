package com.drugsafety.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drugsafety.entity.AdverseReactionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品不良反应记录 Mapper 接口
 */
@Mapper
public interface AdverseReactionRecordMapper extends BaseMapper<AdverseReactionRecord> {
}
