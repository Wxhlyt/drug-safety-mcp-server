package com.drugsafety.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品不良反应记录视图对象
 * 用于返回给前端展示
 */
@Data
@Schema(description = "药品不良反应记录响应数据")
public class AdverseReactionRecordVO {

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
     * 不良反应名称
     */
    @Schema(description = "不良反应名称")
    private String reactionName;

    /**
     * 不良反应描述
     */
    @Schema(description = "不良反应描述")
    private String reactionDescription;

    /**
     * 严重程度
     */
    @Schema(description = "严重程度")
    private String severityLevel;

    /**
     * 不良反应发生时间
     */
    @Schema(description = "不良反应发生时间")
    private LocalDateTime occurrenceTime;

    /**
     * 报告人
     */
    @Schema(description = "报告人")
    private String reporter;

    /**
     * 处理措施
     */
    @Schema(description = "处理措施")
    private String treatment;

    /**
     * 状态
     */
    @Schema(description = "状态：1-有效，0-删除")
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
