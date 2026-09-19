package com.example.projectmanager.user;

import java.util.Set;

public final class RolePermissions {

    private RolePermissions() {
    }

    public static Set<String> defaultsForUser() {
        return Set.of(PermissionIds.PROJECT_VIEW, PermissionIds.TASK_VIEW, PermissionIds.TASK_MANAGE);
    }

    public static Set<String> defaultsForAdmin() {
        return PermissionIds.all();
    }

    public static String defaultName(String id) {
        return PermissionIds.names().getOrDefault(id, id);
    }
}
