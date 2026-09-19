package com.example.taskmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_project_id", columnList = "project_id"),
    @Index(name = "idx_tasks_assigned_user_id", columnList = "assigned_user_id"),
    @Index(name = "idx_tasks_status", columnList = "status"),
    @Index(name = "idx_tasks_priority", columnList = "priority")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isOverdue() {
        return dueDate != null
            && status != Status.COMPLETED
            && status != Status.CANCELLED
            && dueDate.isBefore(LocalDateTime.now());
    }

    public enum Status {
        TODO, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
