package com.example.taskmanagement.service.impl;

import com.example.taskmanagement.dto.request.ProjectRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ForbiddenException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.ProjectService;
import com.example.taskmanagement.util.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
            .name(request.getName())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : Project.Status.ACTIVE)
            .owner(owner)
            .build();

        return mapper.toProjectResponse(projectRepository.save(project));
    }

    @Override
    public PagedResponse<ProjectResponse> getAllProjects(
        Project.Status status, Long ownerId, String search, Pageable pageable
    ) {
        Page<ProjectResponse> page = projectRepository
            .findAll(ProjectSpecification.filter(status, ownerId, search), pageable)
            .map(mapper::toProjectResponse);
        return PagedResponse.from(page);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        return mapper.toProjectResponse(findProject(id));
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String currentUserEmail) {
        Project project = findProject(id);
        assertOwnerOrAdmin(project, currentUserEmail);

        project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());

        return mapper.toProjectResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void deleteProject(Long id, String currentUserEmail) {
        Project project = findProject(id);
        assertOwnerOrAdmin(project, currentUserEmail);
        projectRepository.delete(project);
    }

    @Override
    public List<TaskResponse> getProjectTasks(Long projectId) {
        Project project = findProject(projectId);
        return taskRepository.findByProject(project, Pageable.unpaged()).stream()
            .map(mapper::toTaskResponse)
            .toList();
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private void assertOwnerOrAdmin(Project project, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (currentUser.getRole() != User.Role.ADMIN
            && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to modify this project");
        }
    }
}
