package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.DrugRiskRecordDTO;
import com.drugsafety.entity.DrugRiskRecord;
import com.drugsafety.service.DrugRiskRecordService;
import com.drugsafety.vo.DrugRiskRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品风险评估记录管理控制器
 */
@RestController
@RequestMapping("/drug-risk")
@RequiredArgsConstructor
@Tag(name = "药品风险评估记录管理", description = "药品风险评估记录 CRUD 接口")
public class DrugRiskRecordController {

    private final DrugRiskRecordService drugRiskRecordService;

    /**
     * 将 DTO 转换为实体对象
     */
    private DrugRiskRecord convertToEntity(DrugRiskRecordDTO dto) {
        DrugRiskRecord record = new DrugRiskRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }

    /**
     * 将实体对象转换为 VO
     */
    private DrugRiskRecordVO convertToVO(DrugRiskRecord record) {
        DrugRiskRecordVO vo = new DrugRiskRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询风险记录详情")
    public Result<DrugRiskRecordVO> getById(@PathVariable Long id) {
        DrugRiskRecord record = drugRiskRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "风险记录不存在");
        }
        return Result.success("查询成功", convertToVO(record));
    }

    @PostMapping
    @Operation(summary = "新增风险记录")
    public Result<Void> create(@Valid @RequestBody DrugRiskRecordDTO dto) {
        DrugRiskRecord record = convertToEntity(dto);
        return drugRiskRecordService.save(record) ? Result.success("新增成功") : Result.error(500, "新增失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新风险记录")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DrugRiskRecordDTO dto) {
        DrugRiskRecord existing = drugRiskRecordService.getById(id);
        if (existing == null) {
            return Result.error(404, "风险记录不存在");
        }
        DrugRiskRecord record = convertToEntity(dto);
        record.setId(id);
        return drugRiskRecordService.updateById(record) ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除风险记录")
    public Result<Void> delete(@PathVariable Long id) {
        return drugRiskRecordService.removeById(id) ? Result.success("删除成功") : Result.error(404, "风险记录不存在");
    }

    @GetMapping("/list")
    @Operation(summary = "查询风险记录列表")
    public Result<List<DrugRiskRecordVO>> list() {
        List<DrugRiskRecord> list = drugRiskRecordService.list();
        List<DrugRiskRecordVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }
}
