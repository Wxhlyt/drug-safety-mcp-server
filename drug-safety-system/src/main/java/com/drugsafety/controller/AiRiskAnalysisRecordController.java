package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.AiRiskAnalysisRecordDTO;
import com.drugsafety.entity.AiRiskAnalysisRecord;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.service.AiRiskAnalysisRecordService;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.vo.AiRiskAnalysisRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI风险分析记录管理控制器
 */
@RestController
@RequestMapping("/ai-risk")
@RequiredArgsConstructor
@Tag(name = "AI风险分析记录管理", description = "AI风险分析记录相关接口")
public class AiRiskAnalysisRecordController {

    private final AiRiskAnalysisRecordService aiRiskAnalysisRecordService;
    private final DrugInfoService drugInfoService;

    /**
     * 将 DTO 转换为实体对象
     */
    private AiRiskAnalysisRecord convertToEntity(AiRiskAnalysisRecordDTO dto) {
        AiRiskAnalysisRecord record = new AiRiskAnalysisRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }

    /**
     * 将实体对象转换为 VO
     */
    private AiRiskAnalysisRecordVO convertToVO(AiRiskAnalysisRecord record) {
        AiRiskAnalysisRecordVO vo = new AiRiskAnalysisRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 根据药品信息生成模拟AI风险分析内容
     */
    private void generateAnalysisContent(AiRiskAnalysisRecord record) {
        DrugInfo drugInfo = drugInfoService.getById(record.getDrugId());
        String drugName = drugInfo != null ? drugInfo.getDrugName() : "未知药品";

        String analysisContent = String.format(
                "基于规则分析，药品【%s】当前风险等级为 %s，" +
                        "已关联 %d 条不良反应记录，" +
                        "建议结合临床使用数据进一步评估。",
                drugName,
                drugInfo != null ? drugInfo.getRiskLevel() : "UNKNOWN",
                0
        );
        String riskSuggestion = String.format(
                "建议对药品【%s】加强用药监测，出现异常情况及时上报。",
                drugName
        );

        record.setAnalysisContent(analysisContent);
        record.setRiskSuggestion(riskSuggestion);
        record.setAnalysisTime(LocalDateTime.now());
    }

    @PostMapping("/analyze")
    @Operation(summary = "根据药品信息生成风险分析记录")
    public Result<AiRiskAnalysisRecordVO> analyze(@Valid @RequestBody AiRiskAnalysisRecordDTO dto) {
        DrugInfo drugInfo = drugInfoService.getById(dto.getDrugId());
        if (drugInfo == null) {
            return Result.error(404, "药品不存在");
        }

        AiRiskAnalysisRecord record = convertToEntity(dto);
        generateAnalysisContent(record);

        if (aiRiskAnalysisRecordService.save(record)) {
            return Result.success("分析成功", convertToVO(record));
        }
        return Result.error(500, "分析失败");
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
