package com.example.projectmanager.bug;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBugRequest(
        @NotBlank String projectId,
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description,
        BugSeverity severity
) {
}
