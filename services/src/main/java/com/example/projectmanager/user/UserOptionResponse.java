package com.example.projectmanager.user;

public record UserOptionResponse(String id, String displayName, String username, String email) {

    public static UserOptionResponse from(User user) {
        return new UserOptionResponse(user.getId(), user.getDisplayName(), user.getUsername(), user.getEmail());
    }
}
