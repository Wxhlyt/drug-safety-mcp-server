package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.DrugInfoDTO;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.service.DemoDrugScope;
import com.drugsafety.vo.DrugInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    /** Keeps every locally maintained drug visible while constraining FRDB rows to the demo subset. */
    private void applyDemoDrugScope(LambdaQueryWrapper<DrugInfo> wrapper) {
        wrapper.and(w -> w.ne(DrugInfo::getDataSource, "FRDB")
                .or()
                .in(DrugInfo::getId, DemoDrugScope.drugIds()));
    }

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
        if (Boolean.TRUE.equals(existing.getSourceReadOnly()) || "FRDB".equals(existing.getDataSource())) {
            return Result.error(403, "FRDB 外部来源记录为只读，请在外部证据详情中查看来源数据");
        }
        DrugInfo drugInfo = convertToEntity(dto);
        drugInfo.setId(id);
        return drugInfoService.updateById(drugInfo) ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除药品")
    public Result<Void> delete(@PathVariable Long id) {
        DrugInfo existing = drugInfoService.getById(id);
        if (existing == null) {
            return Result.error(404, "药品不存在");
        }
        if (Boolean.TRUE.equals(existing.getSourceReadOnly()) || "FRDB".equals(existing.getDataSource())) {
            return Result.error(403, "FRDB 外部来源记录为只读，不能删除");
        }
        return drugInfoService.removeById(id) ? Result.success("删除成功") : Result.error(500, "删除失败");
    }

    @GetMapping("/page")
    @Operation(summary = "服务端分页查询药品列表")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String quality) {
        long safePage = Math.max(1, page);
        long safeSize = Math.min(100, Math.max(1, size));
        LambdaQueryWrapper<DrugInfo> wrapper = new LambdaQueryWrapper<>();
        applyDemoDrugScope(wrapper);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(DrugInfo::getDrugName, keyword)
                    .or().like(DrugInfo::getGenericName, keyword)
                    .or().like(DrugInfo::getExternalUnii, keyword));
        }
        if (source != null && !source.isBlank()) {
            wrapper.eq(DrugInfo::getDataSource, source);
        }
        if (quality != null && !quality.isBlank()) {
            wrapper.eq(DrugInfo::getDataQualityStatus, quality);
        }
        wrapper.orderByAsc(DrugInfo::getId);
        Page<DrugInfo> result = drugInfoService.page(new Page<>(safePage, safeSize), wrapper);
        List<DrugInfoVO> items = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", Map.of("items", items, "total", result.getTotal(), "page", safePage, "size", safeSize));
    }

    @GetMapping("/options")
    @Operation(summary = "按药品名称前缀检索候选项")
    public Result<List<DrugInfoVO>> options(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DrugInfo> wrapper = new LambdaQueryWrapper<>();
        applyDemoDrugScope(wrapper);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.likeRight(DrugInfo::getDrugName, keyword);
        }
        wrapper.orderByAsc(DrugInfo::getDrugName).last("LIMIT 20");
        List<DrugInfoVO> items = drugInfoService.list(wrapper).stream()
                .map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", items);
    }

    @GetMapping("/list")
    @Operation(summary = "查询已选药品及本地维护药品列表")
    public Result<List<DrugInfoVO>> list() {
        LambdaQueryWrapper<DrugInfo> wrapper = new LambdaQueryWrapper<>();
        applyDemoDrugScope(wrapper);
        wrapper.orderByAsc(DrugInfo::getId);
        List<DrugInfo> list = drugInfoService.list(wrapper);
        List<DrugInfoVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }
}
