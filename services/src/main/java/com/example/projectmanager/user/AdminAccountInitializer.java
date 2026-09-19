package com.example.projectmanager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        User admin = new User(
                "admin",
                "admin@example.com",
                passwordEncoder.encode("admin"),
                "管理员",
                UserRole.ADMIN
        );
        userRepository.save(admin);
    }
}
