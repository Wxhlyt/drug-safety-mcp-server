package com.drugsafety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drugsafety.entity.DrugRiskRecord;

/**
 * 药品风险评估记录服务接口
 * 后续可扩展：按药品ID查询风险历史、AI风险分析等
 */
public interface DrugRiskRecordService extends IService<DrugRiskRecord> {
}