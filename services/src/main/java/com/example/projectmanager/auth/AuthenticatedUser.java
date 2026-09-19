package com.example.projectmanager.auth;

import com.example.projectmanager.user.User;
import com.example.projectmanager.user.Permission;
import com.example.projectmanager.user.Role;
import java.util.Collection;
import java.util.ArrayList;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    private final String id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final String role;
    private final String roleName;
    private final String roleId;
    private final Collection<Permission> permissions;
    private final int tokenVersion;
    private final boolean enabled;

    private AuthenticatedUser(User user, Role roleDefinition) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.displayName = user.getDisplayName();
        this.role = roleDefinition == null ? user.getLegacyRole() : roleDefinition.getCode();
        this.roleName = roleDefinition == null ? user.getLegacyRole() : roleDefinition.getName();
        this.roleId = roleDefinition == null ? user.getRoleId() : roleDefinition.getId();
        this.permissions = roleDefinition == null ? java.util.Set.of() : roleDefinition.getPermissions();
        this.tokenVersion = user.getTokenVersion();
        this.enabled = user.isEnabled();
    }

    public static AuthenticatedUser from(User user, Role roleDefinition) {
        return new AuthenticatedUser(user, roleDefinition);
    }

    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(user, null);
    }

    public String getId() {
        return id;
    }

    public String getUsernameValue() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleId() {
        return roleId;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new ArrayList<GrantedAuthority>();
        if (role != null && !role.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
        }
        permissions.stream()
                .map(Permission::getId)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
