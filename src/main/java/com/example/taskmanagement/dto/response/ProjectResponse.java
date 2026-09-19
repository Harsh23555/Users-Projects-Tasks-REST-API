package com.example.taskmanagement.dto.response;

import com.example.taskmanagement.entity.Project;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Project.Status status;
    private UserResponse owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long taskCount;
}
