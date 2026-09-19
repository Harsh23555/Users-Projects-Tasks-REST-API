package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.request.UpdateTaskStatusRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Task;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(Long projectId, TaskRequest request, String currentUserEmail);
    PagedResponse<TaskResponse> getAllTasks(Task.Status status, Task.Priority priority,
                                            Long projectId, Long assignedUserId, String search, Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse updateTask(Long id, TaskRequest request, String currentUserEmail);
    void deleteTask(Long id, String currentUserEmail);
    TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request, String currentUserEmail);
    TaskResponse assignTask(Long id, Long userId, String currentUserEmail);
}
