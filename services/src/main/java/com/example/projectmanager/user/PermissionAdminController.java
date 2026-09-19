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
@RequestMapping("/api/permissions")
@PreAuthorize("hasAuthority('" + PermissionIds.ROLE_MANAGE + "')")
public class PermissionAdminController {

    private final PermissionAdminService permissionAdminService;

    public PermissionAdminController(PermissionAdminService permissionAdminService) {
        this.permissionAdminService = permissionAdminService;
    }

    @GetMapping
    public List<PermissionResponse> listPermissions() {
        return permissionAdminService.listPermissions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return permissionAdminService.createPermission(request);
    }

    @PatchMapping("/{id}")
    public PermissionResponse updatePermission(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return permissionAdminService.updatePermission(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable("id") String id) {
        permissionAdminService.deletePermission(id);
    }
}
