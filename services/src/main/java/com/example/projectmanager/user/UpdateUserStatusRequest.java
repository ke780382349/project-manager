package com.example.projectmanager.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull(message = "启用状态不能为空") Boolean enabled) {
}
