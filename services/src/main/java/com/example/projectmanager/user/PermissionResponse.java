package com.example.projectmanager.user;

public record PermissionResponse(
        String id,
        String name,
        String description,
        boolean builtIn
) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.isBuiltIn()
        );
    }
}
