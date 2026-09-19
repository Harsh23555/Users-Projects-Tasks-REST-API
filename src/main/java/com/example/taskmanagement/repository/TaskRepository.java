package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByProject(Project project, Pageable pageable);

    List<Task> findByAssignedUser(User user);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'COMPLETED'")
    long countCompletedTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status IN ('TODO','IN_PROGRESS')")
    long countPendingTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now AND t.status NOT IN ('COMPLETED','CANCELLED')")
    long countOverdueTasks(LocalDateTime now);
}
