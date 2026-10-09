package com.drugsafety.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI风险分析记录视图对象
 * 用于返回给前端展示
 */
@Data
@Schema(description = "AI风险分析记录响应数据")
public class AiRiskAnalysisRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Long id;

    /**
     * 药品ID，关联 drug_info.id
     */
    @Schema(description = "药品ID")
    private Long drugId;

    /**
     * 药品名称（规则分析时一并返回，便于前端展示）
     */
    @Schema(description = "药品名称")
    private String drugName;

    /**
     * 风险等级：LOW(低)/MEDIUM(中)/HIGH(高)
     */
    @Schema(description = "风险等级：LOW(低)/MEDIUM(中)/HIGH(高)")
    private String riskLevel;

    /**
     * 风险评分，范围 0.00-100.00（由后端规则计算，结果可复现）
     */
    @Schema(description = "风险评分")
    private BigDecimal riskScore;

    /**
     * 风险原因说明（基于实际数据生成）
     */
    @Schema(description = "风险原因说明")
    private String riskReason;

    /**
     * 分析内容
     */
    @Schema(description = "分析内容")
    private String analysisContent;

    /**
     * 风险建议
     */
    @Schema(description = "风险建议")
    private String riskSuggestion;

    /**
     * AI分析时间
     */
    @Schema(description = "AI分析时间")
    private LocalDateTime analysisTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
