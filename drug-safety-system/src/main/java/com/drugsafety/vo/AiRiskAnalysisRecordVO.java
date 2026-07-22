package com.drugsafety.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
     * AI分析内容
     */
    @Schema(description = "AI分析内容")
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
