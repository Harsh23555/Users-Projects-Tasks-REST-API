package com.example.taskmanagement.dto.request;

import com.example.taskmanagement.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Task.Status status = Task.Status.TODO;

    private Task.Priority priority = Task.Priority.MEDIUM;

    private LocalDateTime dueDate;

    private Long assignedUserId;
}
