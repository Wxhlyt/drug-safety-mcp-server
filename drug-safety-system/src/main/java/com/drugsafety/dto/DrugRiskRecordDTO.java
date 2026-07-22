package com.drugsafety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品风险评估记录数据传输对象
 * 用于新增、修改风险记录时接收前端参数
 */
@Data
@Schema(description = "药品风险评估记录请求参数")
public class DrugRiskRecordDTO {

    /**
     * 药品ID，关联 drug_info.id
     */
    @NotNull(message = "药品ID不能为空")
    @Schema(description = "药品ID，关联 drug_info.id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long drugId;

    /**
     * 风险等级：LOW(低)/MEDIUM(中)/HIGH(高)/UNKNOWN(未知)
     */
    @NotBlank(message = "风险等级不能为空")
    @Schema(description = "风险等级：LOW/MEDIUM/HIGH/UNKNOWN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevel;

    /**
     * 风险评分，范围 0.00-100.00
     */
    @Schema(description = "风险评分，范围 0.00-100.00")
    private BigDecimal riskScore;

    /**
     * 风险原因说明
     */
    @Schema(description = "风险原因说明")
    private String riskReason;

    /**
     * 风险分析结果
     */
    @Schema(description = "风险分析结果")
    private String analysisResult;

    /**
     * 分析类型：RULE(规则分析) / AI(辅助分析)
     */
    @Schema(description = "分析类型：RULE/AI")
    private String analysisType;
}
