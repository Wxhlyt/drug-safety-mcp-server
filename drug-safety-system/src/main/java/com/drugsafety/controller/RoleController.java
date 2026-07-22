package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.RoleDTO;
import com.drugsafety.entity.Role;
import com.drugsafety.service.RoleService;
import com.drugsafety.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色基础 CRUD 接口")
public class RoleController {

    private final RoleService roleService;

    /**
     * 将 DTO 转换为实体对象
     */
    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        return role;
    }

    /**
     * 将实体对象转换为 VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    public Result<List<RoleVO>> list() {
        List<Role> list = roleService.list();
        List<RoleVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询角色")
    public Result<RoleVO> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return role != null ? Result.success("查询成功", convertToVO(role)) : Result.error(404, "角色不存在");
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Void> create(@Valid @RequestBody RoleDTO dto) {
        Role role = convertToEntity(dto);
        role.setStatus(1);
        return roleService.save(role) ? Result.success("创建成功") : Result.error(500, "创建失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        Role role = convertToEntity(dto);
        role.setId(id);
        return roleService.updateById(role) ? Result.success("更新成功") : Result.error(404, "角色不存在");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        return roleService.removeById(id) ? Result.success("删除成功") : Result.error(404, "角色不存在");
    }
}
