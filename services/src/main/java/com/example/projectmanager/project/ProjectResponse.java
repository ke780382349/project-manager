package com.example.projectmanager.project;

import java.time.Instant;
import java.util.List;

public record ProjectResponse(
        String id,
        String name,
        String description,
        ProjectMemberResponse owner,
        List<ProjectMemberResponse> members,
        Instant createdAt,
        Instant updatedAt
) {
}
