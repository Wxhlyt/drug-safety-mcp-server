package com.drugsafety.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.drugsafety.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI风险分析记录实体类
 * 关联 drug_info，保存AI风险分析结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_risk_analysis_record")
public class AiRiskAnalysisRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 药品ID，关联 drug_info.id
     */
    @TableField("drug_id")
    private Long drugId;

    /**
     * AI分析内容
     */
    @TableField("analysis_content")
    private String analysisContent;

    /**
     * 风险建议
     */
    @TableField("risk_suggestion")
    private String riskSuggestion;

    /**
     * AI分析时间
     */
    @TableField("analysis_time")
    private LocalDateTime analysisTime;
}
