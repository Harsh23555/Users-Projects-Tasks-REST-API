package com.example.taskmanagement.dto.request;

import com.example.taskmanagement.entity.Task;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {

    @NotNull(message = "Status is required")
    private Task.Status status;
}
