package com.example.projectmanager.user;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    public RoleAdminController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + PermissionIds.ROLE_MANAGE + "', '" + PermissionIds.USER_MANAGE + "')")
    public List<RoleResponse> listRoles() {
        return roleAdminService.listRoles();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionIds.ROLE_MANAGE + "')")
    public List<PermissionResponse> listPermissions() {
        return roleAdminService.listPermissions();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionIds.ROLE_MANAGE + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return roleAdminService.createRole(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.ROLE_MANAGE + "')")
    public RoleResponse updateRole(@PathVariable("id") String id,
                                   @Valid @RequestBody UpdateRoleRequest request) {
        return roleAdminService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionIds.ROLE_MANAGE + "')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable("id") String id) {
        roleAdminService.deleteRole(id);
    }
}
