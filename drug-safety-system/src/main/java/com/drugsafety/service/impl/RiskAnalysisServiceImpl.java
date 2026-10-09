package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.entity.AiRiskAnalysisRecord;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.entity.DrugRiskRecord;
import com.drugsafety.exception.BusinessException;
import com.drugsafety.service.AdverseReactionRecordService;
import com.drugsafety.service.AiRiskAnalysisRecordService;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.service.DrugRiskRecordService;
import com.drugsafety.service.RiskAnalysisService;
import com.drugsafety.vo.AiRiskAnalysisRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品风险分析服务实现类
 * 规则化评分：基础分（首条风险记录或药品基础分）+ 重度不良反应×8 + 中度×4，结果可复现
 */
@Service
@RequiredArgsConstructor
public class RiskAnalysisServiceImpl implements RiskAnalysisService {

    private final DrugInfoService drugInfoService;
    private final DrugRiskRecordService drugRiskRecordService;
    private final AdverseReactionRecordService adverseReactionRecordService;
    private final AiRiskAnalysisRecordService aiRiskAnalysisRecordService;

    private static final BigDecimal HIGH_ADVERSE_WEIGHT = BigDecimal.valueOf(8);
    private static final BigDecimal MEDIUM_ADVERSE_WEIGHT = BigDecimal.valueOf(4);
    private static final BigDecimal HIGH_RISK_THRESHOLD = BigDecimal.valueOf(70);
    private static final BigDecimal MEDIUM_RISK_THRESHOLD = BigDecimal.valueOf(40);
    private static final BigDecimal MAX_SCORE = BigDecimal.valueOf(100);
    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiRiskAnalysisRecordVO analyzeDrugRisk(Long drugId) {
        DrugInfo drugInfo = drugInfoService.getById(drugId);
        if (drugInfo == null) {
            return null;
        }
        String drugName = drugInfo.getDrugName();

        // 1. 基础分：取该药品首条风险记录评分；无风险记录则用药品基础分
        List<DrugRiskRecord> riskRecords = drugRiskRecordService.list(
                new LambdaQueryWrapper<DrugRiskRecord>()
                        .eq(DrugRiskRecord::getDrugId, drugId)
                        .orderByAsc(DrugRiskRecord::getId)
        );
        BigDecimal baseScore = (drugInfo.getRiskScore() != null) ? drugInfo.getRiskScore() : BigDecimal.ZERO;
        if (!riskRecords.isEmpty() && riskRecords.get(0).getRiskScore() != null) {
            baseScore = riskRecords.get(0).getRiskScore();
        }

        // 2. 真实查询该药品不良反应记录，统计 HIGH/MEDIUM 数量
        List<AdverseReactionRecord> adverseRecords = adverseReactionRecordService.list(
                new LambdaQueryWrapper<AdverseReactionRecord>()
                        .eq(AdverseReactionRecord::getDrugId, drugId)
        );
        if ("FRDB".equalsIgnoreCase(drugInfo.getDataSource())
                && riskRecords.isEmpty() && adverseRecords.isEmpty()) {
            throw new BusinessException(400, "该药品只有来源库原始证据，尚无已核实的风险或不良反应记录，不能据此生成风险评分");
        }
        long highCount = adverseRecords.stream()
                .filter(r -> "HIGH".equals(r.getSeverityLevel())).count();
        long mediumCount = adverseRecords.stream()
                .filter(r -> "MEDIUM".equals(r.getSeverityLevel())).count();

        // 3. 计算最终评分（无随机波动，可复现），钳制 0~100
        BigDecimal adjustment = BigDecimal.valueOf(highCount).multiply(HIGH_ADVERSE_WEIGHT)
                .add(BigDecimal.valueOf(mediumCount).multiply(MEDIUM_ADVERSE_WEIGHT));
        BigDecimal riskScore = baseScore.add(adjustment);
        riskScore = riskScore.max(MIN_SCORE).min(MAX_SCORE).setScale(1, RoundingMode.HALF_UP);

        // 4. 判定风险等级
        String riskLevel;
        if (riskScore.compareTo(HIGH_RISK_THRESHOLD) >= 0) {
            riskLevel = "HIGH";
        } else if (riskScore.compareTo(MEDIUM_RISK_THRESHOLD) >= 0) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        // 5. 生成风险原因（与实际数据一致）
        String levelLabel = riskLevelLabel(riskLevel);
        String topThreeNames = adverseRecords.stream()
                .map(AdverseReactionRecord::getReactionName)
                .filter(name -> name != null && !name.isEmpty())
                .limit(3)
                .collect(Collectors.joining("、"));
        String adverseText = adverseRecords.isEmpty()
                ? "暂无不良反应记录"
                : String.format("已关联 %d 条不良反应记录（%s）", adverseRecords.size(), topThreeNames);
        String riskReason = String.format(
                "根据药品【%s】信息和历史不良反应记录分析，%s，该药品存在%s风险。",
                drugName, adverseText, levelLabel.replace("风险", "")
        );

        // 6. 生成分析内容与风险建议
        String analysisContent = String.format(
                "基于规则分析，药品【%s】当前风险等级为 %s，风险评分为 %s，" +
                        "已关联 %d 条不良反应记录（其中重度 %d 条、中度 %d 条），" +
                        "建议结合临床使用数据进一步评估。",
                drugName, riskLevel, riskScore.toString(),
                adverseRecords.size(), highCount, mediumCount
        );
        String riskSuggestion;
        if ("HIGH".equals(riskLevel)) {
            riskSuggestion = String.format(
                    "药品【%s】为高风险药品，建议严格监测用药情况，重点关注患者不良反应并及时上报。", drugName);
        } else if ("MEDIUM".equals(riskLevel)) {
            riskSuggestion = String.format(
                    "药品【%s】为中风险药品，建议加强用药监测，出现异常情况及时上报。", drugName);
        } else {
            riskSuggestion = String.format(
                    "药品【%s】为低风险药品，建议按常规流程用药并留意异常反应。", drugName);
        }

        // 7. 保存分析记录到数据库（含风险等级与评分，与本次计算结果一致，不重复计算）
        AiRiskAnalysisRecord record = new AiRiskAnalysisRecord();
        record.setDrugId(drugId);
        record.setRiskLevel(riskLevel);
        record.setRiskScore(riskScore);
        record.setAnalysisContent(analysisContent);
        record.setRiskSuggestion(riskSuggestion);
        record.setAnalysisTime(LocalDateTime.now());
        boolean saved = aiRiskAnalysisRecordService.save(record);
        if (!saved) {
            throw new BusinessException("风险分析结果保存失败");
        }

        // 8. 组装返回结果
        AiRiskAnalysisRecordVO vo = new AiRiskAnalysisRecordVO();
        vo.setId(record.getId());
        vo.setDrugId(drugId);
        vo.setDrugName(drugName);
        vo.setRiskLevel(riskLevel);
        vo.setRiskScore(riskScore);
        vo.setRiskReason(riskReason);
        vo.setAnalysisContent(analysisContent);
        vo.setRiskSuggestion(riskSuggestion);
        vo.setAnalysisTime(record.getAnalysisTime());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        return vo;
    }

    private String riskLevelLabel(String level) {
        if ("HIGH".equals(level)) return "高风险";
        if ("MEDIUM".equals(level)) return "中风险";
        return "低风险";
    }
}
