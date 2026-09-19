package com.example.projectmanager.user;

import com.example.projectmanager.auth.AuthenticatedUser;
import java.util.List;

public record UserResponse(
        String id,
        String username,
        String email,
        String displayName,
        String roleId,
        String role,
        String roleName,
        List<String> permissions,
        boolean enabled
) {

    public static UserResponse from(AuthenticatedUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsernameValue(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRoleId(),
                user.getRole(),
                user.getRoleName(),
                user.getPermissions().stream().map(Permission::getId).sorted().toList(),
                user.isEnabled()
        );
    }

    public static UserResponse from(User user, Role roleDefinition) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                roleDefinition == null ? user.getRoleId() : roleDefinition.getId(),
                roleDefinition == null ? user.getLegacyRole() : roleDefinition.getCode(),
                roleDefinition == null ? "" : roleDefinition.getName(),
                roleDefinition == null ? List.of() : roleDefinition.getPermissions().stream().map(Permission::getId).sorted().toList(),
                user.isEnabled()
        );
    }
}
