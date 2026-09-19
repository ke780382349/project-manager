package com.example.projectmanager.bug;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBugRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description,
        @NotNull BugSeverity severity,
        @NotNull BugStatus status
) {
}
