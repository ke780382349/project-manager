package com.example.projectmanager.requirement;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementRepository extends JpaRepository<Requirement, String> {

    List<Requirement> findByProjectIdInOrderByCreatedAtDesc(Collection<String> projectIds);
}
