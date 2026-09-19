package com.example.projectmanager.web;

import com.example.projectmanager.auth.AuthenticatedUser;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public Map<String, String> dashboard(@AuthenticationPrincipal AuthenticatedUser user) {
        return Map.of(
                "message", "欢迎进入项目管理系统",
                "displayName", user.getDisplayName()
        );
    }
}
