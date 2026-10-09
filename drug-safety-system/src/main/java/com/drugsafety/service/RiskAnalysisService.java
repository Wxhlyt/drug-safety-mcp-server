package com.drugsafety.service;

import com.drugsafety.vo.AiRiskAnalysisRecordVO;

/**
 * 药品风险分析服务接口
 * 基于历史风险记录与不良反应数据进行规则化评分，不调用任何大模型
 */
public interface RiskAnalysisService {

    /**
     * 根据药品ID执行风险分析
     * 计算流程：取药品首条风险记录评分（无则取药品基础分）→ 按不良反应严重程度加权 → 钳制 0~100 → 判定等级
     *
     * @param drugId 药品ID
     * @return 风险分析结果（药品不存在时返回 null）
     */
    AiRiskAnalysisRecordVO analyzeDrugRisk(Long drugId);
}