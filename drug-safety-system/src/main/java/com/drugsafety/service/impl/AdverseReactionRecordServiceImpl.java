package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.mapper.AdverseReactionRecordMapper;
import com.drugsafety.service.AdverseReactionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 药品不良反应记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdverseReactionRecordServiceImpl extends ServiceImpl<AdverseReactionRecordMapper, AdverseReactionRecord> implements AdverseReactionRecordService {

    private final AdverseReactionRecordMapper adverseReactionRecordMapper;
}
