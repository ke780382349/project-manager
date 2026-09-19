package com.example.projectmanager.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleAdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public RoleAdminService(RoleRepository roleRepository, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAllByOrderByBuiltInDescNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAllByOrderByBuiltInDescNameAsc().stream()
                .map(PermissionResponse::from)
                .toList();
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        Role role = new Role(request.name().trim(), cleanDescription(request.description()), resolvePermissions(request.permissionIds()));
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(String id, UpdateRoleRequest request) {
        Role role = getRole(id);
        if (role.isBuiltIn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内置角色不能修改");
        }
        role.setName(request.name().trim());
        role.setDescription(cleanDescription(request.description()));
        role.setPermissions(resolvePermissions(request.permissionIds()));
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(String id) {
        Role role = getRole(id);
        if (role.isBuiltIn()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内置角色不能删除");
        }
        long userCount = userRepository.countByRoleId(role.getId());
        if (userCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "角色已分配给用户，不能删除");
        }
        roleRepository.delete(role);
    }

    private RoleResponse toResponse(Role role) {
        Set<Permission> permissions = role.getPermissions();
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions.stream()
                        .map(PermissionResponse::from)
                        .sorted(java.util.Comparator.comparing(PermissionResponse::id))
                        .toList(),
                role.isBuiltIn(),
                userRepository.countByRoleId(role.getId())
        );
    }

    private Role getRole(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在"));
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private Set<Permission> resolvePermissions(Set<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Set.of();
        }
        Set<String> ids = new HashSet<>();
        for (String permissionId : permissionIds) {
            if (permissionId != null && !permissionId.isBlank()) {
                ids.add(permissionId.trim());
            }
        }
        List<Permission> permissions = permissionRepository.findAllById(ids);
        if (permissions.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "包含不存在的权限");
        }
        return Set.copyOf(permissions);
    }
}
