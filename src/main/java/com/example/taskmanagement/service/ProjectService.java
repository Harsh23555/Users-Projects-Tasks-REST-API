package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.ProjectRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Project;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request, String ownerEmail);
    PagedResponse<ProjectResponse> getAllProjects(Project.Status status, Long ownerId, String search, Pageable pageable);
    ProjectResponse getProjectById(Long id);
    ProjectResponse updateProject(Long id, ProjectRequest request, String currentUserEmail);
    void deleteProject(Long id, String currentUserEmail);
    List<TaskResponse> getProjectTasks(Long projectId);
}
