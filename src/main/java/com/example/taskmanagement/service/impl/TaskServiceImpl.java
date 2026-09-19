package com.example.taskmanagement.service.impl;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.request.UpdateTaskStatusRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ForbiddenException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.TaskService;
import com.example.taskmanagement.util.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request, String currentUserEmail) {
        Project project = findProject(projectId);
        User currentUser = findUserByEmail(currentUserEmail);
        assertProjectOwnerOrAdmin(project, currentUser);

        User assignee = null;
        if (request.getAssignedUserId() != null) {
            assignee = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedUserId()));
        }

        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : Task.Status.TODO)
            .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
            .dueDate(request.getDueDate())
            .project(project)
            .assignedUser(assignee)
            .build();

        return mapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    public PagedResponse<TaskResponse> getAllTasks(
        Task.Status status, Task.Priority priority,
        Long projectId, Long assignedUserId, String search, Pageable pageable
    ) {
        Page<TaskResponse> page = taskRepository
            .findAll(TaskSpecification.filter(status, priority, projectId, assignedUserId, search), pageable)
            .map(mapper::toTaskResponse);
        return PagedResponse.from(page);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        return mapper.toTaskResponse(findTask(id));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String currentUserEmail) {
        Task task = findTask(id);
        User currentUser = findUserByEmail(currentUserEmail);
        assertProjectOwnerOrAdmin(task.getProject(), currentUser);

        task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        if (request.getAssignedUserId() != null) {
            User assignee = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedUserId()));
            task.setAssignedUser(assignee);
        }

        return mapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long id, String currentUserEmail) {
        Task task = findTask(id);
        User currentUser = findUserByEmail(currentUserEmail);
        assertProjectOwnerOrAdmin(task.getProject(), currentUser);
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request, String currentUserEmail) {
        Task task = findTask(id);
        User currentUser = findUserByEmail(currentUserEmail);

        // Project owner, ADMIN, or assigned user can update status
        boolean isOwnerOrAdmin = currentUser.getRole() == User.Role.ADMIN
            || task.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedUser() != null
            && task.getAssignedUser().getId().equals(currentUser.getId());

        if (!isOwnerOrAdmin && !isAssignee) {
            throw new ForbiddenException("You do not have permission to update this task's status");
        }

        task.setStatus(request.getStatus());
        return mapper.toTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long id, Long userId, String currentUserEmail) {
        Task task = findTask(id);
        User currentUser = findUserByEmail(currentUserEmail);
        assertProjectOwnerOrAdmin(task.getProject(), currentUser);

        User assignee = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        task.setAssignedUser(assignee);
        return mapper.toTaskResponse(taskRepository.save(task));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void assertProjectOwnerOrAdmin(Project project, User user) {
        if (user.getRole() != User.Role.ADMIN
            && !project.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to modify tasks in this project");
        }
    }
}
