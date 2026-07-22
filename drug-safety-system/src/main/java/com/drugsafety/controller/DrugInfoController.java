package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.DrugInfoDTO;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.vo.DrugInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品基础信息管理控制器
 */
@RestController
@RequestMapping("/drug")
@RequiredArgsConstructor
@Tag(name = "药品基础信息管理", description = "药品基础信息 CRUD 接口")
public class DrugInfoController {

    private final DrugInfoService drugInfoService;

    /**
     * 将 DTO 转换为实体对象
     */
    private DrugInfo convertToEntity(DrugInfoDTO dto) {
        DrugInfo drugInfo = new DrugInfo();
        BeanUtils.copyProperties(dto, drugInfo);
        return drugInfo;
    }

    /**
     * 将实体对象转换为 VO
     */
    private DrugInfoVO convertToVO(DrugInfo drugInfo) {
        DrugInfoVO vo = new DrugInfoVO();
        BeanUtils.copyProperties(drugInfo, vo);
        return vo;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询药品详情")
    public Result<DrugInfoVO> getById(@PathVariable Long id) {
        DrugInfo drugInfo = drugInfoService.getById(id);
        if (drugInfo == null) {
            return Result.error(404, "药品不存在");
        }
        return Result.success("查询成功", convertToVO(drugInfo));
    }

    @PostMapping
    @Operation(summary = "新增药品")
    public Result<Void> create(@Valid @RequestBody DrugInfoDTO dto) {
        DrugInfo drugInfo = convertToEntity(dto);
        drugInfo.setStatus(1);
        drugInfo.setRiskLevel("UNKNOWN");
        drugInfo.setRiskScore(BigDecimal.ZERO);
        return drugInfoService.save(drugInfo) ? Result.success("新增成功") : Result.error(500, "新增失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新药品信息")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DrugInfoDTO dto) {
        DrugInfo existing = drugInfoService.getById(id);
        if (existing == null) {
            return Result.error(404, "药品不存在");
        }
        DrugInfo drugInfo = convertToEntity(dto);
        drugInfo.setId(id);
        return drugInfoService.updateById(drugInfo) ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    public Result<Void> delete(@PathVariable Long id) {
        return drugInfoService.removeById(id) ? Result.success("删除成功") : Result.error(404, "药品不存在");
    }

    @GetMapping("/list")
    @Operation(summary = "查询药品列表")
    public Result<List<DrugInfoVO>> list() {
        List<DrugInfo> list = drugInfoService.list();
        List<DrugInfoVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }
}