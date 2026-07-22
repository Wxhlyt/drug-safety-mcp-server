package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.UserDTO;
import com.drugsafety.dto.UserRoleAssignDTO;
import com.drugsafety.entity.Role;
import com.drugsafety.entity.User;
import com.drugsafety.service.UserService;
import com.drugsafety.vo.RoleVO;
import com.drugsafety.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户基础 CRUD 及角色查询接口")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 将 DTO 转换为实体对象
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return user;
    }

    /**
     * 将实体对象转换为 VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 将角色实体对象转换为 VO
     */
    private RoleVO convertRoleToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    @GetMapping("/list")
    @Operation(summary = "查询用户列表")
    public Result<List<UserVO>> list() {
        List<User> list = userService.list();
        List<UserVO> voList = list.stream().map(this::convertToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? Result.success("查询成功", convertToVO(user)) : Result.error(404, "用户不存在");
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<UserVO> create(@Valid @RequestBody UserDTO dto) {
        User user = convertToEntity(dto);
        user.setStatus(1);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (userService.save(user)) {
            return Result.success("创建成功", convertToVO(user));
        }
        return Result.error(500, "创建失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User user = convertToEntity(dto);
        user.setId(id);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return userService.updateById(user) ? Result.success("更新成功") : Result.error(404, "用户不存在");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success("删除成功") : Result.error(404, "用户不存在");
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "查询用户角色列表")
    public Result<List<RoleVO>> getUserRoles(@PathVariable Long id) {
        List<Role> roles = userService.getRolesByUserId(id);
        List<RoleVO> voList = roles.stream().map(this::convertRoleToVO).collect(Collectors.toList());
        return Result.success("查询成功", voList);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "分配用户角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        log.info("分配用户角色请求(PUT): userId={}, roleIds={}", id, roleIds);
        if (userService.getById(id) == null) {
            return Result.error(404, "用户不存在");
        }
        userService.assignRoles(id, roleIds);
        return Result.success("角色分配成功");
    }

    @PostMapping("/assignRoles")
    @Operation(summary = "分配用户角色")
    public Result<Void> assignRoles(@Valid @RequestBody UserRoleAssignDTO dto) {
        System.out.println("===== assignRoles 接口被调用 =====");
        System.out.println("userId=" + dto.getUserId());
        System.out.println("roleIds=" + dto.getRoleIds());
        if (userService.getById(dto.getUserId()) == null) {
            System.out.println("用户不存在");
            return Result.error(404, "用户不存在");
        }
        userService.assignRoles(dto.getUserId(), dto.getRoleIds());
        System.out.println("===== assignRoles 接口完成 =====");
        return Result.success("角色分配成功");
    }
}
