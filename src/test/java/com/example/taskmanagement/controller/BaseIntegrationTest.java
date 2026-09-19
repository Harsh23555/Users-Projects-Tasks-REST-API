package com.example.taskmanagement.controller;

import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProjectRepository projectRepository;
    @Autowired protected TaskRepository taskRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtService jwtService;

    protected User regularUser;
    protected User adminUser;
    protected User otherUser;
    protected Project project;
    protected Task task;

    @BeforeEach
    void setUpBase() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        regularUser = userRepository.save(User.builder()
            .name("John Doe").email("john@example.com")
            .password(passwordEncoder.encode("password123"))
            .role(User.Role.USER).build());

        adminUser = userRepository.save(User.builder()
            .name("Admin").email("admin@example.com")
            .password(passwordEncoder.encode("admin123"))
            .role(User.Role.ADMIN).build());

        otherUser = userRepository.save(User.builder()
            .name("Jane").email("jane@example.com")
            .password(passwordEncoder.encode("password123"))
            .role(User.Role.USER).build());

        project = projectRepository.save(Project.builder()
            .name("Test Project").description("A test project")
            .status(Project.Status.ACTIVE).owner(regularUser).build());

        task = taskRepository.save(Task.builder()
            .title("Test Task").status(Task.Status.TODO)
            .priority(Task.Priority.MEDIUM).project(project).build());
    }

    protected String tokenFor(User user) {
        UserDetails ud = org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .build();
        return "Bearer " + jwtService.generateToken(ud);
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
