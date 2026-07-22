package com.drugsafety.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private Integer status;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}