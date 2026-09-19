package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.request.UpdateTaskStatusRequest;
import com.example.taskmanagement.dto.response.*;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    // Create task within a project
    @PostMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Create a task in a project")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
        @PathVariable Long projectId,
        @Valid @RequestBody TaskRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponse task = taskService.createTask(projectId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task created successfully", task));
    }

    // List tasks for a project
    @GetMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Get all tasks for a project")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getProjectTasks(
        @PathVariable Long projectId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(
            taskService.getAllTasks(null, null, projectId, null, null, pageable)));
    }

    // Global task listing with filters
    @GetMapping("/api/tasks")
    @Operation(summary = "Get all tasks with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getAllTasks(
        @RequestParam(required = false) Task.Status status,
        @RequestParam(required = false) Task.Priority priority,
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) Long assignedUserId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(
            taskService.getAllTasks(status, priority, projectId, assignedUserId, search, pageable)));
    }

    @GetMapping("/api/tasks/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @PutMapping("/api/tasks/{id}")
    @Operation(summary = "Update task (project owner or ADMIN)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
        @PathVariable Long id,
        @Valid @RequestBody TaskRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponse updated = taskService.updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updated));
    }

    @DeleteMapping("/api/tasks/{id}")
    @Operation(summary = "Delete task (project owner or ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PatchMapping("/api/tasks/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskStatusRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponse updated = taskService.updateTaskStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task status updated", updated));
    }

    @PatchMapping("/api/tasks/{id}/assign/{userId}")
    @Operation(summary = "Assign task to a user")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
        @PathVariable Long id,
        @PathVariable Long userId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponse updated = taskService.assignTask(id, userId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully", updated));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
