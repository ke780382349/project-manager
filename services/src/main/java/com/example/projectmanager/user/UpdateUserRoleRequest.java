package com.example.projectmanager.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(
        @NotBlank(message = "角色不能为空")
        String roleId
) {
}
