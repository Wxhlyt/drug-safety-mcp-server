package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.AiRiskAnalysisRecordDTO;
import com.drugsafety.entity.AiRiskAnalysisRecord;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.service.AiRiskAnalysisRecordService;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.service.RiskAnalysisService;
import com.drugsafety.service.SourceEvidenceAnalysisService;
import com.drugsafety.vo.AiRiskAnalysisRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 风险分析记录管理控制器
 */
@RestController
@RequestMapping("/ai-risk")
@RequiredArgsConstructor
@Tag(name = "风险分析记录管理", description = "风险分析记录相关接口")
public class AiRiskAnalysisRecordController {

    private final AiRiskAnalysisRecordService aiRiskAnalysisRecordService;
    private final RiskAnalysisService riskAnalysisService;
    private final DrugInfoService drugInfoService;
    private final SourceEvidenceAnalysisService sourceEvidenceAnalysisService;

    /**
     * 将实体对象转换为 VO
     */
    private AiRiskAnalysisRecordVO convertToVO(AiRiskAnalysisRecord record) {
        AiRiskAnalysisRecordVO vo = new AiRiskAnalysisRecordVO();
        BeanUtils.copyProperties(record, vo);
        // 补充药品名称：drugName 存储于 drug_info 表，需根据 drugId 单独查询
        if (record.getDrugId() != null) {
            DrugInfo drugInfo = drugInfoService.getById(record.getDrugId());
            if (drugInfo != null) {
                vo.setDrugName(drugInfo.getDrugName());
            }
        }
        return vo;
    }

    @PostMapping("/analyze")
    @Operation(summary = "根据药品信息执行规则化风险分析")
    public Result<AiRiskAnalysisRecordVO> analyze(@Valid @RequestBody AiRiskAnalysisRecordDTO dto) {
        AiRiskAnalysisRecordVO vo = riskAnalysisService.analyzeDrugRisk(dto.getDrugId());
        if (vo == null) {
            return Result.error(404, "药品不存在");
        }
        return Result.success("分析成功", vo);
    }

    @GetMapping("/{drugId}/source-analysis")
    @Operation(summary = "按固定规则汇总药品的 FRDB 原始来源证据，不生成临床风险评分")
    public Result<Map<String, Object>> sourceAnalysis(@PathVariable Long drugId) {
        Map<String, Object> result = sourceEvidenceAnalysisService.analyze(drugId);
        return result == null
                ? Result.error(404, "未找到该药品的 FRDB 来源记录")
                : Result.success("分析成功", result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询AI分析详情")
    public Result<AiRiskAnalysisRecordVO> getById(@PathVariable Long id) {
        AiRiskAnalysisRecord record = aiRiskAnalysisRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "AI分析记录不存在");
        }
        return Result.success("查询成功", convertToVO(record));
    }

    @GetMapping("/list")
    @Operation(summary = "查询AI分析历史列表")
    public Result<List<AiRiskAnalysisRecordVO>> list() {
        List<AiRiskAnalysisRecord> list = aiRiskAnalysisRecordService.list();
        List<AiRiskAnalysisRecordVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除AI分析记录")
    public Result<Void> delete(@PathVariable Long id) {
        return aiRiskAnalysisRecordService.removeById(id) ? Result.success("删除成功") : Result.error(404, "AI分析记录不存在");
    }
}
