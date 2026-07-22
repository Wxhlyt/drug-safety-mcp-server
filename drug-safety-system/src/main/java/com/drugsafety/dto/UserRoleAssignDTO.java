package com.drugsafety.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<Long> roleIds;
}
