package com.drugsafety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI风险分析记录数据传输对象
 * 用于新增AI风险分析记录时接收前端参数
 */
@Data
@Schema(description = "AI风险分析记录请求参数")
public class AiRiskAnalysisRecordDTO {

    /**
     * 药品ID，关联 drug_info.id
     */
    @NotNull(message = "药品ID不能为空")
    @Schema(description = "药品ID，关联 drug_info.id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long drugId;

    /**
     * AI分析内容
     */
    @Schema(description = "AI分析内容")
    private String analysisContent;

    /**
     * 风险建议
     */
    @Schema(description = "风险建议")
    private String riskSuggestion;
}
