package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.AdverseReactionRecordDTO;
import com.drugsafety.entity.DrugInfo;
import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.service.AdverseReactionRecordService;
import com.drugsafety.service.DrugInfoService;
import com.drugsafety.vo.AdverseReactionRecordVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 药品不良反应记录管理控制器
 */
@RestController
@RequestMapping("/adverse-reaction")
@RequiredArgsConstructor
@Tag(name = "药品不良反应记录管理", description = "药品不良反应记录 CRUD 接口")
public class AdverseReactionRecordController {

    private final AdverseReactionRecordService adverseReactionRecordService;
    private final DrugInfoService drugInfoService;

    /**
     * 将 DTO 转换为实体对象
     */
    private AdverseReactionRecord convertToEntity(AdverseReactionRecordDTO dto) {
        AdverseReactionRecord record = new AdverseReactionRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }

    /**
     * 将实体对象转换为 VO
     */
    private AdverseReactionRecordVO convertToVO(AdverseReactionRecord record, String drugName) {
        AdverseReactionRecordVO vo = new AdverseReactionRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setDrugName(drugName);
        return vo;
    }

    private Map<Long, DrugInfo> loadDrugs(List<Long> drugIds) {
        if (drugIds.isEmpty()) {
            return Map.of();
        }
        return drugInfoService.listByIds(drugIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(DrugInfo::getId, Function.identity()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询不良反应记录详情")
    public Result<AdverseReactionRecordVO> getById(@PathVariable Long id) {
        AdverseReactionRecord record = adverseReactionRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "不良反应记录不存在");
        }
        DrugInfo drug = drugInfoService.getById(record.getDrugId());
        return Result.success("查询成功", convertToVO(record, drug == null ? null : drug.getDrugName()));
    }

    @PostMapping
    @Operation(summary = "新增不良反应记录")
    public Result<Void> create(@Valid @RequestBody AdverseReactionRecordDTO dto) {
        AdverseReactionRecord record = convertToEntity(dto);
        record.setStatus(1);
        return adverseReactionRecordService.save(record) ? Result.success("新增成功") : Result.error(500, "新增失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新不良反应记录")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AdverseReactionRecordDTO dto) {
        AdverseReactionRecord existing = adverseReactionRecordService.getById(id);
        if (existing == null) {
            return Result.error(404, "不良反应记录不存在");
        }
        AdverseReactionRecord record = convertToEntity(dto);
        record.setId(id);
        return adverseReactionRecordService.updateById(record) ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除不良反应记录")
    public Result<Void> delete(@PathVariable Long id) {
        return adverseReactionRecordService.removeById(id) ? Result.success("删除成功") : Result.error(404, "不良反应记录不存在");
    }

    @GetMapping("/list")
    @Operation(summary = "查询不良反应记录列表")
    public Result<List<AdverseReactionRecordVO>> list() {
        List<AdverseReactionRecord> list = adverseReactionRecordService.list();
        Map<Long, DrugInfo> drugs = loadDrugs(list.stream().map(AdverseReactionRecord::getDrugId).toList());
        List<AdverseReactionRecordVO> voList = list.stream()
                .map(record -> convertToVO(record, drugs.containsKey(record.getDrugId())
                        ? drugs.get(record.getDrugId()).getDrugName() : null))
                .collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @GetMapping("/page")
    @Operation(summary = "服务端分页查询人工或系统不良反应记录")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        long safePage = Math.max(1, page);
        long safeSize = Math.min(100, Math.max(1, size));
        LambdaQueryWrapper<AdverseReactionRecord> wrapper = new LambdaQueryWrapper<>();
        // External FRDB evidence is intentionally available only from the
        // read-only drug detail endpoint, never from this business-record list.
        wrapper.and(w -> w.isNull(AdverseReactionRecord::getReporter)
                .or().ne(AdverseReactionRecord::getReporter, "NCATS FRDB"));
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(AdverseReactionRecord::getReactionName, keyword)
                    .or().like(AdverseReactionRecord::getReporter, keyword));
        }
        wrapper.orderByDesc(AdverseReactionRecord::getCreateTime);
        Page<AdverseReactionRecord> result = adverseReactionRecordService.page(new Page<>(safePage, safeSize), wrapper);
        Map<Long, DrugInfo> drugs = loadDrugs(result.getRecords().stream().map(AdverseReactionRecord::getDrugId).toList());
        List<AdverseReactionRecordVO> items = result.getRecords().stream()
                .map(record -> convertToVO(record, drugs.containsKey(record.getDrugId())
                        ? drugs.get(record.getDrugId()).getDrugName() : null))
                .collect(Collectors.toList());
        return Result.success("查询成功", Map.of("items", items, "total", result.getTotal(), "page", safePage, "size", safeSize));
    }
}
