package com.drugsafety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 药品基础信息数据传输对象
 * 用于新增、修改药品时接收前端参数
 */
@Data
@Schema(description = "药品基础信息请求参数")
public class DrugInfoDTO {

    /**
     * 药品名称（商品名）
     */
    @NotBlank(message = "药品名称不能为空")
    @Schema(description = "药品名称（商品名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String drugName;

    /**
     * 通用名称
     */
    @NotBlank(message = "通用名称不能为空")
    @Schema(description = "通用名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String genericName;

    /**
     * 药品分类
     */
    @Schema(description = "药品分类，如抗生素、降压药")
    private String drugCategory;

    /**
     * 药品分类编码
     */
    @Schema(description = "药品分类编码，用于分类统计")
    private String categoryCode;

    /**
     * 剂型
     */
    @Schema(description = "剂型，如片剂、胶囊、注射液")
    private String dosageForm;

    /**
     * 药品规格
     */
    @Schema(description = "药品规格，如 500mg/片")
    private String specification;

    /**
     * 储存条件
     */
    @Schema(description = "储存条件，如常温、2-8℃冷藏")
    private String storageCondition;

    /**
     * 生产厂家
     */
    @Schema(description = "生产厂家")
    private String manufacturer;

    /**
     * 批准文号
     */
    @Schema(description = "批准文号，如国药准字H20240001")
    private String approvalNumber;

    /**
     * 药品描述
     */
    @Schema(description = "药品描述、适应症、用法用量等")
    private String drugDescription;
}