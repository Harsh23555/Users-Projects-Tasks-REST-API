package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.UpdateUserRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request, String currentUserEmail);
    void deleteUser(Long id);
    List<ProjectResponse> getUserProjects(Long id);
    List<TaskResponse> getUserTasks(Long id);
}
