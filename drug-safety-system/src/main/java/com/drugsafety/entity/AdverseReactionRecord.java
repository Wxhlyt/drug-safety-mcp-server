package com.drugsafety.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.drugsafety.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 药品不良反应记录实体类
 * 关联 drug_info，记录药品不良反应信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("adverse_reaction_record")
public class AdverseReactionRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 药品ID，关联 drug_info.id
     */
    @TableField("drug_id")
    private Long drugId;

    /**
     * 不良反应名称
     */
    @TableField("reaction_name")
    private String reactionName;

    /**
     * 不良反应描述
     */
    @TableField("reaction_description")
    private String reactionDescription;

    /**
     * 严重程度：LOW(低)/MEDIUM(中)/HIGH(高)
     */
    @TableField("severity_level")
    private String severityLevel;

    /**
     * 不良反应发生时间
     */
    @TableField("occurrence_time")
    private LocalDateTime occurrenceTime;

    /**
     * 报告人
     */
    @TableField("reporter")
    private String reporter;

    /**
     * 处理措施
     */
    @TableField("treatment")
    private String treatment;

    /**
     * 状态：1-有效，0-删除
     */
    @TableField("status")
    private Integer status;

}
