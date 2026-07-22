package com.drugsafety.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.drugsafety.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 药品基础信息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drug_info")
public class DrugInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 药品名称（商品名）
     */
    @TableField("drug_name")
    private String drugName;

    /**
     * 通用名称
     */
    @TableField("generic_name")
    private String genericName;

    /**
     * 药品分类：如抗生素、降压药、镇痛药等
     */
    @TableField("drug_category")
    private String drugCategory;

    /**
     * 药品分类编码，用于分类统计和系统内部标识
     */
    @TableField("category_code")
    private String categoryCode;

    /**
     * 剂型，例如片剂、胶囊、注射液
     */
    @TableField("dosage_form")
    private String dosageForm;

    /**
     * 药品规格，例如 500mg/片、10ml:0.1g
     */
    @TableField("specification")
    private String specification;

    /**
     * 储存条件，例如常温、2-8℃冷藏、避光保存
     */
    @TableField("storage_condition")
    private String storageCondition;

    /**
     * 生产厂家
     */
    @TableField("manufacturer")
    private String manufacturer;

    /**
     * 批准文号，如国药准字H20240001
     */
    @TableField("approval_number")
    private String approvalNumber;

    /**
     * 药品描述、适应症、用法用量等
     */
    @TableField("drug_description")
    private String drugDescription;

    /**
     * 风险等级：LOW(低)/MEDIUM(中)/HIGH(高)/UNKNOWN(未知)
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险评分，0.00-100.00，用于AI风险分析排序
     */
    @TableField("risk_score")
    private BigDecimal riskScore;

    /**
     * 已知不良反应记录，JSON或文本格式
     */
    @TableField("adverse_reactions")
    private String adverseReactions;

    /**
     * AI风险分析结果摘要
     */
    @TableField("ai_risk_analysis")
    private String aiRiskAnalysis;

    /**
     * 状态：1-启用，0-禁用
     */
    @TableField("status")
    private Integer status;
}