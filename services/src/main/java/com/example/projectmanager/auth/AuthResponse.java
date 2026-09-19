package com.example.projectmanager.auth;

import com.example.projectmanager.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
