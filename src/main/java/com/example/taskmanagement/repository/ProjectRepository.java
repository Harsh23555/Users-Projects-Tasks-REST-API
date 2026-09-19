package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Page<Project> findByOwner(User owner, Pageable pageable);

    List<Project> findByOwner(User owner);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'ACTIVE'")
    long countActiveProjects();
}
