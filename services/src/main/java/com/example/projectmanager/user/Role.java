package com.example.projectmanager.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_code", columnNames = "code"))
public class Role {

    @Id
    @Column(length = 32, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_role_permissions", columnNames = {"role_id", "permission_id"})
    )
    private Set<Permission> permissions = new HashSet<>();

    protected Role() {
    }

    public Role(String code, String name, String description, boolean builtIn, Set<Permission> permissions) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.builtIn = builtIn;
        setPermissions(permissions);
    }

    public Role(String name, String description, Set<Permission> permissions) {
        this("ROLE_" + compactUuid().substring(0, 16), name, description, false, permissions);
    }

    @jakarta.persistence.PrePersist
    void assignId() {
        if (id == null) {
            id = compactUuid();
        }
    }

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public Set<Permission> getPermissions() {
        return Set.copyOf(permissions);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions == null ? new HashSet<>() : new HashSet<>(permissions);
    }
}
