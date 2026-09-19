package com.example.projectmanager.requirement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequirementRequest(
        @NotBlank String projectId,
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description
) {
}
