package com.example.projectmanager.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 80, message = "角色名称不能超过 80 个字符")
        String name,
        @Size(max = 255, message = "角色描述不能超过 255 个字符")
        String description,
        @NotNull(message = "权限不能为空")
        Set<String> permissionIds
) {
}
