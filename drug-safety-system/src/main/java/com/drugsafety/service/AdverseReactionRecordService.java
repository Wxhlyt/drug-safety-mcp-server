package com.drugsafety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drugsafety.entity.AdverseReactionRecord;

/**
 * 药品不良反应记录服务接口
 * 后续可扩展：按药品ID查询不良反应记录、按严重程度统计等
 */
public interface AdverseReactionRecordService extends IService<AdverseReactionRecord> {
}
