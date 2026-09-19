package com.example.projectmanager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Order(1)
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Permission> permissions = ensureDefaultPermissions();
        ensureBuiltInRole("ADMIN", "管理员", "系统管理员，拥有全部权限", resolvePermissions(permissions, RolePermissions.defaultsForAdmin()));
        ensureBuiltInRole("USER", "普通用户", "普通工作区成员", resolvePermissions(permissions, RolePermissions.defaultsForUser()));
        Role userRole = roleRepository.findByCodeIgnoreCase("USER").orElseThrow();
        for (User user : userRepository.findAll()) {
            if (user.getRoleId() == null) {
                Role role = roleRepository.findByCodeIgnoreCase(user.getLegacyRole() == null ? "USER" : user.getLegacyRole())
                        .orElse(userRole);
                user.setRoleId(role.getId());
                userRepository.save(user);
            }
        }
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        Role adminRole = roleRepository.findByCodeIgnoreCase("ADMIN").orElseThrow();
        User admin = new User(
                "admin",
                "admin@example.com",
                passwordEncoder.encode("admin"),
                "管理员",
                adminRole.getId()
        );
        userRepository.save(admin);
    }

    private Map<String, Permission> ensureDefaultPermissions() {
        Set<String> ids = new HashSet<>(RolePermissions.defaultsForAdmin());
        ids.addAll(RolePermissions.defaultsForUser());
        Map<String, Permission> permissions = new HashMap<>();
        for (String id : ids) {
            Permission permission = permissionRepository.findById(id)
                    .orElseGet(() -> permissionRepository.save(new Permission(
                            id,
                            RolePermissions.defaultName(id),
                            "系统内置权限",
                            true
                    )));
            permissions.put(id, permission);
        }
        return permissions;
    }

    private Set<Permission> resolvePermissions(Map<String, Permission> permissions, Set<String> ids) {
        return ids.stream().map(permissions::get).collect(java.util.stream.Collectors.toSet());
    }

    private void ensureBuiltInRole(String code, String name, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> new Role(code, name, description, true, permissions));
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}
