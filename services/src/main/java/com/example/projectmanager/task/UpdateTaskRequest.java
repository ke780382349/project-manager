package com.example.projectmanager.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description,
        @NotNull TaskStatus status,
        String assigneeId
) {
}
