package com.example.projectmanager.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RolePermissionsTest {

    @Test
    void adminHasEveryDefinedPermission() {
        assertTrue(RolePermissions.defaultsForAdmin().contains(PermissionIds.USER_MANAGE));
        assertTrue(RolePermissions.defaultsForAdmin().contains(PermissionIds.ROLE_MANAGE));
    }

    @Test
    void regularUserCannotManageUsers() {
        assertTrue(RolePermissions.defaultsForUser().contains(PermissionIds.PROJECT_VIEW));
        assertFalse(RolePermissions.defaultsForUser().contains(PermissionIds.USER_MANAGE));
    }
}
