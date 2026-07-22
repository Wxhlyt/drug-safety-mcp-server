package com.drugsafety.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.mapper.DrugInfoMapper;
import com.drugsafety.service.DrugInfoService;
import org.springframework.stereotype.Service;

/**
 * 药品基础信息服务实现类
 */
@Service
public class DrugInfoServiceImpl extends ServiceImpl<DrugInfoMapper, DrugInfo> implements DrugInfoService {
}