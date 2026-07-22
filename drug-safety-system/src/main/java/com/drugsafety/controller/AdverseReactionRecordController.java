package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.AdverseReactionRecordDTO;
import com.drugsafety.entity.AdverseReactionRecord;
import com.drugsafety.service.AdverseReactionRecordService;
import com.drugsafety.vo.AdverseReactionRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private AdverseReactionRecordVO convertToVO(AdverseReactionRecord record) {
        AdverseReactionRecordVO vo = new AdverseReactionRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询不良反应记录详情")
    public Result<AdverseReactionRecordVO> getById(@PathVariable Long id) {
        AdverseReactionRecord record = adverseReactionRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "不良反应记录不存在");
        }
        return Result.success("查询成功", convertToVO(record));
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
        List<AdverseReactionRecordVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }
}
