package com.drugsafety.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.drugsafety.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 药品风险评估记录实体类
 * 关联 drug_info，保存药品每次风险评估的历史记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_risk_record")
public class DrugRiskRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 药品ID，关联 drug_info.id
     */
    @TableField("drug_id")
    private Long drugId;

    /**
     * 风险等级：LOW(低)/MEDIUM(中)/HIGH(高)/UNKNOWN(未知)
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险评分，范围 0.00-100.00
     */
    @TableField("risk_score")
    private BigDecimal riskScore;

    /**
     * 风险原因说明
     */
    @TableField("risk_reason")
    private String riskReason;

    /**
     * 风险分析结果
     */
    @TableField("analysis_result")
    private String analysisResult;

    /**
     * 分析类型：RULE(规则分析) / AI(辅助分析)
     */
    @TableField("analysis_type")
    private String analysisType;
}