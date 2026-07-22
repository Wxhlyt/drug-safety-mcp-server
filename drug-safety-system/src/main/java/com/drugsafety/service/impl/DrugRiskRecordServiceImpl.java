package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.DrugRiskRecord;
import com.drugsafety.mapper.DrugRiskRecordMapper;
import com.drugsafety.service.DrugRiskRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 药品风险评估记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class DrugRiskRecordServiceImpl extends ServiceImpl<DrugRiskRecordMapper, DrugRiskRecord> implements DrugRiskRecordService {

    private final DrugRiskRecordMapper drugRiskRecordMapper;
}
