package com.example.taskmanagement.dto.response;

import com.example.taskmanagement.entity.Task;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private LocalDateTime dueDate;
    private Long projectId;
    private String projectName;
    private UserResponse assignedUser;
    private boolean overdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
