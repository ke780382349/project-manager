package com.example.projectmanager.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank(message = "权限名称不能为空")
        @Size(max = 80, message = "权限名称不能超过 80 个字符")
        String name,
        @Size(max = 255, message = "权限描述不能超过 255 个字符")
        String description
) {
}
