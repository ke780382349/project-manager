package com.example.projectmanager.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @Column(length = 32, updatable = false)
    private String id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    protected Permission() {
    }

    public Permission(String name, String description, boolean builtIn) {
        this.name = name;
        this.description = description;
        this.builtIn = builtIn;
    }

    public Permission(String id, String name, String description, boolean builtIn) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.builtIn = builtIn;
    }

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }
    }

    public String getId() {
        return id;
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
}
