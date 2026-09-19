package com.example.projectmanager.project;

import jakarta.validation.constraints.NotBlank;

public record AddProjectMemberRequest(@NotBlank String userId) {
}
