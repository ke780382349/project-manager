package com.example.projectmanager.bug;

import java.time.Instant;

public record BugResponse(
        String id,
        String projectId,
        String title,
        String description,
        BugSeverity severity,
        BugStatus status,
        String reporterId,
        String reporterName,
        Instant createdAt,
        Instant updatedAt
) {
}
