package com.example.projectmanager.requirement;

import java.time.Instant;

public record RequirementResponse(
        String id,
        String projectId,
        String projectName,
        String title,
        String description,
        RequirementStatus status,
        String creatorId,
        String creatorName,
        Instant createdAt,
        Instant updatedAt
) {
}
