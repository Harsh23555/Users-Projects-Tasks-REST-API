package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.ProjectRequest;
import com.example.taskmanagement.dto.response.*;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.service.ProjectService;
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

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
        @Valid @RequestBody ProjectRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponse project = projectService.createProject(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping
    @Operation(summary = "Get all projects with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getAllProjects(
        @RequestParam(required = false) Project.Status status,
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(
            projectService.getAllProjects(status, ownerId, search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project (owner or ADMIN)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
        @PathVariable Long id,
        @Valid @RequestBody ProjectRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponse updated = projectService.updateProject(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project (owner or ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get all tasks for a project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectTasks(id)));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
