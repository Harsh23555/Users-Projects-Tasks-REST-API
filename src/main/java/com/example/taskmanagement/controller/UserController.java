package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.UpdateUserRequest;
import com.example.taskmanagement.dto.response.*;
import com.example.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse updated = userService.updateUser(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/{id}/projects")
    @Operation(summary = "Get all projects owned by a user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjects(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProjects(id)));
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get all tasks assigned to a user")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUserTasks(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserTasks(id)));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, parts[0]));
    }
}
