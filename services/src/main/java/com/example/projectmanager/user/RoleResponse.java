package com.example.projectmanager.user;

import java.util.List;

public record RoleResponse(
        String id,
        String name,
        String description,
        List<PermissionResponse> permissions,
        boolean builtIn,
        long userCount
) {
}
