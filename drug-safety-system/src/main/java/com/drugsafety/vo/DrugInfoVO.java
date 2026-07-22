package com.drugsafety.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品基础信息视图对象
 * 用于返回给前端展示
 */
@Data
@Schema(description = "药品基础信息响应数据")
public class DrugInfoVO {

    /**
     * 药品ID
     */
    @Schema(description = "药品ID")
    private Long id;

    /**
     * 药品名称（商品名）
     */
    @Schema(description = "药品名称（商品名）")
    private String drugName;

    /**
     * 通用名称
     */
    @Schema(description = "通用名称")
    private String genericName;

    /**
     * 药品分类
     */
    @Schema(description = "药品分类")
    private String drugCategory;

    /**
     * 药品分类编码
     */
    @Schema(description = "药品分类编码")
    private String categoryCode;

    /**
     * 剂型
     */
    @Schema(description = "剂型")
    private String dosageForm;

    /**
     * 药品规格
     */
    @Schema(description = "药品规格")
    private String specification;

    /**
     * 储存条件
     */
    @Schema(description = "储存条件")
    private String storageCondition;

    /**
     * 生产厂家
     */
    @Schema(description = "生产厂家")
    private String manufacturer;

    /**
     * 批准文号
     */
    @Schema(description = "批准文号")
    private String approvalNumber;

    /**
     * 药品描述
     */
    @Schema(description = "药品描述")
    private String drugDescription;

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
     * 状态
     */
    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

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