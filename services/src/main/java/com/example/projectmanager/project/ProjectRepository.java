package com.example.projectmanager.project;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, String> {

    @Query("select distinct p from Project p left join p.members member where p.ownerId = :userId or member.id = :userId order by p.updatedAt desc")
    List<Project> findVisibleFor(@Param("userId") String userId);

    List<Project> findAllByOrderByUpdatedAtDesc();

}
