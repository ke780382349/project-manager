package com.example.projectmanager.user;

public record UserResponse(Long id, String email, String displayName, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole().name());
    }
}
