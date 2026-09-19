package com.example.projectmanager.user;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermissionAdminService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionAdminService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAllByOrderByBuiltInDescNameAsc().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        Permission permission = new Permission(request.name().trim(), cleanDescription(request.description()), false);
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponse updatePermission(String id, UpdatePermissionRequest request) {
        Permission permission = getPermission(id);
        if (permission.isBuiltIn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内置权限不能修改");
        }
        permission.setName(request.name().trim());
        permission.setDescription(cleanDescription(request.description()));
        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(String id) {
        Permission permission = getPermission(id);
        if (permission.isBuiltIn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内置权限不能删除");
        }
        roleRepository.findAll().stream()
                .filter(role -> role.getPermissions().stream().anyMatch(item -> item.getId().equals(id)))
                .findAny()
                .ifPresent(role -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "权限已分配给角色，不能删除");
                });
        permissionRepository.delete(permission);
    }

    private Permission getPermission(String id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "权限不存在"));
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
