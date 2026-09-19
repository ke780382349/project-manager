package com.example.projectmanager.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {

    @Id
    @Column(length = 32, updatable = false)
    private String id;

    @Column(length = 80, unique = true)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "role_id", length = 32)
    private String roleId;

    // 兼容开发库中旧的角色编码，启动时会迁移到 role_id。
    @Column(name = "role", length = 80)
    private String legacyRole;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(String email, String passwordHash, String displayName) {
        this(null, email, passwordHash, displayName, null);
    }

    public User(String username, String email, String passwordHash, String displayName, String roleId) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.roleId = roleId;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getLegacyRole() {
        return legacyRole;
    }

    public void setLegacyRole(String legacyRole) {
        this.legacyRole = legacyRole;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            invalidateTokens();
        }
    }

    public void invalidateTokens() {
        tokenVersion++;
    }
}
