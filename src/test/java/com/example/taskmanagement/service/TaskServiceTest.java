package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.TaskRequest;
import com.example.taskmanagement.dto.request.UpdateTaskStatusRequest;
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
import com.example.taskmanagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock EntityMapper mapper;

    @InjectMocks TaskServiceImpl taskService;

    private User owner;
    private User otherUser;
    private User adminUser;
    private User assignee;
    private Project project;
    private Task task;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("john@example.com").role(User.Role.USER).build();
        otherUser = User.builder().id(2L).email("jane@example.com").role(User.Role.USER).build();
        adminUser = User.builder().id(99L).email("admin@example.com").role(User.Role.ADMIN).build();
        assignee = User.builder().id(3L).email("dev@example.com").role(User.Role.USER).build();

        project = Project.builder().id(1L).name("Project A")
            .status(Project.Status.ACTIVE).owner(owner).build();

        task = Task.builder().id(1L).title("Fix Bug")
            .status(Task.Status.TODO).priority(Task.Priority.HIGH)
            .project(project).build();

        taskResponse = TaskResponse.builder().id(1L).title("Fix Bug")
            .status(Task.Status.TODO).priority(Task.Priority.HIGH).build();
    }

    @Test
    void createTask_ownerSuccess() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Fix Bug");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any())).thenReturn(task);
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(1L, req, "john@example.com");
        assertThat(result.getTitle()).isEqualTo("Fix Bug");
    }

    @Test
    void createTask_nonOwnerThrowsForbidden() {
        TaskRequest req = new TaskRequest();
        req.setTitle("X");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> taskService.createTask(1L, req, "jane@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createTask_projectNotFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.createTask(99L, new TaskRequest(), "john@example.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTasks_withFilters() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        var result = taskService.getAllTasks(Task.Status.TODO, null, null, null, null, Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getTaskById_found() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        assertThat(taskService.getTaskById(1L).getTitle()).isEqualTo("Fix Bug");
    }

    @Test
    void getTaskById_notFound_throws() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTaskById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTaskStatus_ownerCanUpdate() {
        UpdateTaskStatusRequest req = new UpdateTaskStatusRequest();
        req.setStatus(Task.Status.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any())).thenReturn(task);
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        assertThatCode(() -> taskService.updateTaskStatus(1L, req, "john@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void updateTaskStatus_assigneeCanUpdate() {
        task.setAssignedUser(assignee);
        UpdateTaskStatusRequest req = new UpdateTaskStatusRequest();
        req.setStatus(Task.Status.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any())).thenReturn(task);
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        assertThatCode(() -> taskService.updateTaskStatus(1L, req, "dev@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void updateTaskStatus_nonOwnerNonAssigneeThrowsForbidden() {
        UpdateTaskStatusRequest req = new UpdateTaskStatusRequest();
        req.setStatus(Task.Status.COMPLETED);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> taskService.updateTaskStatus(1L, req, "jane@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void assignTask_ownerCanAssign() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(userRepository.findById(3L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any())).thenReturn(task);
        when(mapper.toTaskResponse(task)).thenReturn(taskResponse);

        assertThatCode(() -> taskService.assignTask(1L, 3L, "john@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void assignTask_invalidUser_throws() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTask(1L, 999L, "john@example.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTask_ownerCanDelete() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));

        assertThatCode(() -> taskService.deleteTask(1L, "john@example.com"))
            .doesNotThrowAnyException();
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_nonOwnerThrowsForbidden() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> taskService.deleteTask(1L, "jane@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }
}
