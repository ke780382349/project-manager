package com.example.projectmanager.task;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByProjectIdInOrderByCreatedAtDesc(Collection<String> projectIds);
}
