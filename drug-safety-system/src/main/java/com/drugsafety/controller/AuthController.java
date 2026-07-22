package com.drugsafety.controller;

import com.drugsafety.common.Result;
import com.drugsafety.dto.LoginDTO;
import com.drugsafety.entity.Role;
import com.drugsafety.entity.User;
import com.drugsafety.service.UserService;
import com.drugsafety.utils.JwtUtil;
import com.drugsafety.vo.LoginVO;
import com.drugsafety.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证控制器，提供登录相关接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录认证相关接口")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        User user = userService.getByUsername(loginDTO.getUsername());

        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            return Result.error(403, "账号已被禁用");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        // 查询用户角色列表
        List<Role> roles = userService.getRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRoles(roleCodes);

        LoginVO loginVO = new LoginVO(token, userVO);
        return Result.success("登录成功", loginVO);
    }
}