package com.drugsafety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品不良反应记录数据传输对象
 * 用于新增、修改不良反应记录时接收前端参数
 */
@Data
@Schema(description = "药品不良反应记录请求参数")
public class AdverseReactionRecordDTO {

    /**
     * 药品ID，关联 drug_info.id
     */
    @NotNull(message = "药品ID不能为空")
    @Schema(description = "药品ID，关联 drug_info.id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long drugId;

    /**
     * 不良反应名称
     */
    @NotBlank(message = "不良反应名称不能为空")
    @Schema(description = "不良反应名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reactionName;

    /**
     * 不良反应描述
     */
    @Schema(description = "不良反应描述")
    private String reactionDescription;

    /**
     * 严重程度：LOW(低)/MEDIUM(中)/HIGH(高)
     */
    @Schema(description = "严重程度：LOW/MEDIUM/HIGH")
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
}
