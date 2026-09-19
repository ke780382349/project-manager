package com.example.projectmanager.task;

import java.time.Instant;

public record TaskResponse(
        String id,
        String projectId,
        String title,
        String description,
        TaskStatus status,
        String assigneeId,
        String assigneeName,
        String creatorId,
        String creatorName,
        Instant createdAt,
        Instant updatedAt
) {
}
