package com.example.projectmanager.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Role> findAllByOrderByBuiltInDescNameAsc();
}
