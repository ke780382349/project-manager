package com.example.projectmanager.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @JsonAlias({"email", "username"})
        @NotBlank(message = "账号不能为空")
        String account,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
