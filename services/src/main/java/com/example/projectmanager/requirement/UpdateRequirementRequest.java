package com.example.projectmanager.requirement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequirementRequest(
        @NotBlank @Size(max = 80) String title,
        @Size(max = 500) String description,
        @NotNull RequirementStatus status
) {
}
