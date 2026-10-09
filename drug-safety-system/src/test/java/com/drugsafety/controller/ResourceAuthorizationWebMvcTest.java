package com.drugsafety.controller;

import com.drugsafety.config.JwtAuthenticationFilter;
import com.drugsafety.config.SecurityConfig;
import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.entity.DrugRiskRecord;
import com.drugsafety.service.AdverseReactionRecordService;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.service.DrugRiskRecordService;
import com.drugsafety.service.UserService;
import com.drugsafety.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 验证三类基础资源接口的 Spring Security 授权边界。
 * JWT 过滤器使用真实实现；无 Authorization 请求会直接放行到 SecurityContext 中的测试身份。
 */
@WebMvcTest(controllers = {
        DrugInfoController.class,
        DrugRiskRecordController.class,
        AdverseReactionRecordController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ResourceAuthorizationWebMvcTest {

    private static final String DRUG_JSON = """
            {"drugName":"测试药品","genericName":"测试通用名","dosageForm":"片剂"}
            """;
    private static final String DRUG_RISK_JSON = """
            {"drugId":1,"riskLevel":"LOW","riskScore":10.0,"analysisType":"RULE"}
            """;
    private static final String ADVERSE_REACTION_JSON = """
            {"drugId":1,"reactionName":"轻微头晕","severityLevel":"LOW"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrugInfoService drugInfoService;
    @MockBean
    private DrugRiskRecordService drugRiskRecordService;
    @MockBean
    private AdverseReactionRecordService adverseReactionRecordService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtil jwtUtil;

    @Test @WithMockUser(roles = "USER")
    void userCanReadDrugList() throws Exception {
        when(drugInfoService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<DrugInfo>>any()))
                .thenReturn(List.of());
        mockMvc.perform(get("/drug/list")).andExpect(status().isOk());
        verify(drugInfoService).list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<DrugInfo>>any());
    }

    @Test @WithMockUser(roles = "USER")
    void userCanReadDrugRiskList() throws Exception {
        when(drugRiskRecordService.list()).thenReturn(List.of());
        mockMvc.perform(get("/drug-risk/list")).andExpect(status().isOk());
        verify(drugRiskRecordService).list();
    }

    @Test @WithMockUser(roles = "USER")
    void riskListIncludesDrugNameFromDatabase() throws Exception {
        DrugRiskRecord record = new DrugRiskRecord();
        record.setDrugId(1550L);
        DrugInfo drug = new DrugInfo();
        drug.setId(1550L);
        drug.setDrugName("WARFARIN");
        when(drugRiskRecordService.list()).thenReturn(List.of(record));
        when(drugInfoService.listByIds(anyCollection())).thenReturn(List.of(drug));

        mockMvc.perform(get("/drug-risk/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].drugName").value("WARFARIN"));
    }

    @Test @WithMockUser(roles = "USER")
    void userCanReadAdverseReactionList() throws Exception {
        when(adverseReactionRecordService.list()).thenReturn(List.of());
        mockMvc.perform(get("/adverse-reaction/list")).andExpect(status().isOk());
        verify(adverseReactionRecordService).list();
    }

    @Test @WithMockUser(roles = "USER")
    void adversePageIncludesDrugNameFromDatabase() throws Exception {
        AdverseReactionRecord record = new AdverseReactionRecord();
        record.setDrugId(1550L);
        DrugInfo drug = new DrugInfo();
        drug.setId(1550L);
        drug.setDrugName("WARFARIN");
        Page<AdverseReactionRecord> page = new Page<>(1, 10);
        page.setRecords(List.of(record));
        page.setTotal(1);
        when(adverseReactionRecordService.page(any(Page.class), org.mockito.ArgumentMatchers.<Wrapper<AdverseReactionRecord>>any()))
                .thenReturn(page);
        when(drugInfoService.listByIds(anyCollection())).thenReturn(List.of(drug));

        mockMvc.perform(get("/adverse-reaction/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].drugName").value("WARFARIN"));
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotCreateDrug() throws Exception {
        mockMvc.perform(post("/drug").contentType(MediaType.APPLICATION_JSON).content(DRUG_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(drugInfoService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotCreateDrugRisk() throws Exception {
        mockMvc.perform(post("/drug-risk").contentType(MediaType.APPLICATION_JSON).content(DRUG_RISK_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(drugRiskRecordService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotCreateAdverseReaction() throws Exception {
        mockMvc.perform(post("/adverse-reaction").contentType(MediaType.APPLICATION_JSON).content(ADVERSE_REACTION_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(adverseReactionRecordService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotUpdateDrug() throws Exception {
        mockMvc.perform(put("/drug/1").contentType(MediaType.APPLICATION_JSON).content(DRUG_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(drugInfoService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotUpdateDrugRisk() throws Exception {
        mockMvc.perform(put("/drug-risk/1").contentType(MediaType.APPLICATION_JSON).content(DRUG_RISK_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(drugRiskRecordService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotUpdateAdverseReaction() throws Exception {
        mockMvc.perform(put("/adverse-reaction/1").contentType(MediaType.APPLICATION_JSON).content(ADVERSE_REACTION_JSON)).andExpect(status().isForbidden());
        verifyNoInteractions(adverseReactionRecordService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotDeleteDrug() throws Exception {
        mockMvc.perform(delete("/drug/1")).andExpect(status().isForbidden());
        verifyNoInteractions(drugInfoService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotDeleteDrugRisk() throws Exception {
        mockMvc.perform(delete("/drug-risk/1")).andExpect(status().isForbidden());
        verifyNoInteractions(drugRiskRecordService);
    }

    @Test @WithMockUser(roles = "USER")
    void userCannotDeleteAdverseReaction() throws Exception {
        mockMvc.perform(delete("/adverse-reaction/1")).andExpect(status().isForbidden());
        verifyNoInteractions(adverseReactionRecordService);
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanCreateDrug() throws Exception {
        when(drugInfoService.save(any(DrugInfo.class))).thenReturn(true);
        mockMvc.perform(post("/drug").contentType(MediaType.APPLICATION_JSON).content(DRUG_JSON)).andExpect(status().is(not(403)));
        verify(drugInfoService).save(any(DrugInfo.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanCreateDrugRisk() throws Exception {
        when(drugRiskRecordService.save(any(DrugRiskRecord.class))).thenReturn(true);
        mockMvc.perform(post("/drug-risk").contentType(MediaType.APPLICATION_JSON).content(DRUG_RISK_JSON)).andExpect(status().is(not(403)));
        verify(drugRiskRecordService).save(any(DrugRiskRecord.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanCreateAdverseReaction() throws Exception {
        when(adverseReactionRecordService.save(any(AdverseReactionRecord.class))).thenReturn(true);
        mockMvc.perform(post("/adverse-reaction").contentType(MediaType.APPLICATION_JSON).content(ADVERSE_REACTION_JSON)).andExpect(status().is(not(403)));
        verify(adverseReactionRecordService).save(any(AdverseReactionRecord.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanUpdateDrug() throws Exception {
        when(drugInfoService.getById(1L)).thenReturn(new DrugInfo());
        when(drugInfoService.updateById(any(DrugInfo.class))).thenReturn(true);
        mockMvc.perform(put("/drug/1").contentType(MediaType.APPLICATION_JSON).content(DRUG_JSON)).andExpect(status().is(not(403)));
        verify(drugInfoService).updateById(any(DrugInfo.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanUpdateDrugRisk() throws Exception {
        when(drugRiskRecordService.getById(1L)).thenReturn(new DrugRiskRecord());
        when(drugRiskRecordService.updateById(any(DrugRiskRecord.class))).thenReturn(true);
        mockMvc.perform(put("/drug-risk/1").contentType(MediaType.APPLICATION_JSON).content(DRUG_RISK_JSON)).andExpect(status().is(not(403)));
        verify(drugRiskRecordService).updateById(any(DrugRiskRecord.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanUpdateAdverseReaction() throws Exception {
        when(adverseReactionRecordService.getById(1L)).thenReturn(new AdverseReactionRecord());
        when(adverseReactionRecordService.updateById(any(AdverseReactionRecord.class))).thenReturn(true);
        mockMvc.perform(put("/adverse-reaction/1").contentType(MediaType.APPLICATION_JSON).content(ADVERSE_REACTION_JSON)).andExpect(status().is(not(403)));
        verify(adverseReactionRecordService).updateById(any(AdverseReactionRecord.class));
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanDeleteDrug() throws Exception {
        // 本地维护记录可删除；FRDB 只读记录的拒绝逻辑由控制器保留。
        when(drugInfoService.getById(1L)).thenReturn(new DrugInfo());
        when(drugInfoService.removeById(1L)).thenReturn(true);
        mockMvc.perform(delete("/drug/1")).andExpect(status().is(not(403)));
        verify(drugInfoService).removeById(1L);
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanDeleteDrugRisk() throws Exception {
        when(drugRiskRecordService.removeById(1L)).thenReturn(true);
        mockMvc.perform(delete("/drug-risk/1")).andExpect(status().is(not(403)));
        verify(drugRiskRecordService).removeById(1L);
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanDeleteAdverseReaction() throws Exception {
        when(adverseReactionRecordService.removeById(1L)).thenReturn(true);
        mockMvc.perform(delete("/adverse-reaction/1")).andExpect(status().is(not(403)));
        verify(adverseReactionRecordService).removeById(1L);
    }
}
