package com.drugsafety.service;

import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.entity.DrugRiskRecord;
import com.drugsafety.exception.BusinessException;
import com.drugsafety.service.impl.RiskAnalysisServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAnalysisSourceDataTest {

    @Mock private DrugInfoService drugInfoService;
    @Mock private DrugRiskRecordService drugRiskRecordService;
    @Mock private AdverseReactionRecordService adverseReactionRecordService;
    @Mock private AiRiskAnalysisRecordService aiRiskAnalysisRecordService;
    @InjectMocks private RiskAnalysisServiceImpl service;

    @Test
    void sourceOnlyDrugCannotBecomeLowRiskFromDefaultZero() {
        DrugInfo drug = new DrugInfo();
        drug.setId(26L);
        drug.setDrugName("BELINOSTAT");
        drug.setDataSource("FRDB");
        when(drugInfoService.getById(26L)).thenReturn(drug);
        when(drugRiskRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(adverseReactionRecordService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        BusinessException error = assertThrows(BusinessException.class, () -> service.analyzeDrugRisk(26L));

        assertEquals(400, error.getCode());
        verifyNoInteractions(aiRiskAnalysisRecordService);
    }
}
