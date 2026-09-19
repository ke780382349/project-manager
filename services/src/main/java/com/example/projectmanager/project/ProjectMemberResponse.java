package com.example.projectmanager.project;

import com.example.projectmanager.user.User;

public record ProjectMemberResponse(String id, String displayName, String username, String email, boolean owner) {

    public static ProjectMemberResponse from(User user, String ownerId) {
        return new ProjectMemberResponse(
                user.getId(),
                user.getDisplayName(),
                user.getUsername(),
                user.getEmail(),
                user.getId().equals(ownerId)
        );
    }
}
