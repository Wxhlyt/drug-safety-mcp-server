package com.drugsafety.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品风险评估记录视图对象
 * 用于返回给前端展示
 */
@Data
@Schema(description = "药品风险评估记录响应数据")
public class DrugRiskRecordVO {

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

    @Schema(description = "药品名称")
    private String drugName;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private String riskLevel;

    /**
     * 风险评分
     */
    @Schema(description = "风险评分")
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
     * 分析类型
     */
    @Schema(description = "分析类型")
    private String analysisType;

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
