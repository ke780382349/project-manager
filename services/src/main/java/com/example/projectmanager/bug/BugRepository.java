package com.example.projectmanager.bug;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugRepository extends JpaRepository<Bug, String> {

    List<Bug> findByProjectIdInOrderByCreatedAtDesc(Collection<String> projectIds);
}
