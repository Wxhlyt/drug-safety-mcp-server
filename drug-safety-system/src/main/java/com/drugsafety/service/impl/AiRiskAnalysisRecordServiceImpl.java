package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.AiRiskAnalysisRecord;
import com.drugsafety.mapper.AiRiskAnalysisRecordMapper;
import com.drugsafety.service.AiRiskAnalysisRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI风险分析记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AiRiskAnalysisRecordServiceImpl extends ServiceImpl<AiRiskAnalysisRecordMapper, AiRiskAnalysisRecord> implements AiRiskAnalysisRecordService {

    private final AiRiskAnalysisRecordMapper aiRiskAnalysisRecordMapper;
}
