package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.ProjectRequest;
import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ForbiddenException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.impl.ProjectServiceImpl;
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
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock TaskRepository taskRepository;
    @Mock EntityMapper mapper;

    @InjectMocks ProjectServiceImpl projectService;

    private User owner;
    private User otherUser;
    private User adminUser;
    private Project project;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("John").email("john@example.com")
            .role(User.Role.USER).build();
        otherUser = User.builder().id(2L).name("Jane").email("jane@example.com")
            .role(User.Role.USER).build();
        adminUser = User.builder().id(99L).name("Admin").email("admin@example.com")
            .role(User.Role.ADMIN).build();

        project = Project.builder().id(1L).name("Project A")
            .status(Project.Status.ACTIVE).owner(owner).build();

        projectResponse = ProjectResponse.builder().id(1L).name("Project A")
            .status(Project.Status.ACTIVE).build();
    }

    @Test
    void createProject_success() {
        ProjectRequest req = new ProjectRequest();
        req.setName("Project A");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any())).thenReturn(project);
        when(mapper.toProjectResponse(project)).thenReturn(projectResponse);

        ProjectResponse result = projectService.createProject(req, "john@example.com");
        assertThat(result.getName()).isEqualTo("Project A");
    }

    @Test
    void createProject_userNotFound_throws() {
        ProjectRequest req = new ProjectRequest();
        req.setName("X");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(req, "nobody@example.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProjects_returnsPaged() {
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toProjectResponse(project)).thenReturn(projectResponse);

        var result = projectService.getAllProjects(null, null, null, Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getProjectById_found() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(mapper.toProjectResponse(project)).thenReturn(projectResponse);

        assertThat(projectService.getProjectById(1L).getName()).isEqualTo("Project A");
    }

    @Test
    void getProjectById_notFound_throws() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.getProjectById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProject_ownerCanUpdate() {
        ProjectRequest req = new ProjectRequest();
        req.setName("Updated");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any())).thenReturn(project);
        when(mapper.toProjectResponse(project)).thenReturn(projectResponse);

        assertThatCode(() -> projectService.updateProject(1L, req, "john@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void updateProject_nonOwnerThrowsForbidden() {
        ProjectRequest req = new ProjectRequest();
        req.setName("Hack");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> projectService.updateProject(1L, req, "jane@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateProject_adminCanUpdate() {
        ProjectRequest req = new ProjectRequest();
        req.setName("Admin Edit");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(projectRepository.save(any())).thenReturn(project);
        when(mapper.toProjectResponse(project)).thenReturn(projectResponse);

        assertThatCode(() -> projectService.updateProject(1L, req, "admin@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void deleteProject_ownerCanDelete() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(owner));

        assertThatCode(() -> projectService.deleteProject(1L, "john@example.com"))
            .doesNotThrowAnyException();
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_nonOwnerThrowsForbidden() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> projectService.deleteProject(1L, "jane@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }
}
