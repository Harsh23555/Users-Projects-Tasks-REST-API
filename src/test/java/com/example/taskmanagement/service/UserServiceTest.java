package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.UpdateUserRequest;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.ForbiddenException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock ProjectRepository projectRepository;
    @Mock TaskRepository taskRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EntityMapper mapper;

    @InjectMocks UserServiceImpl userService;

    private User user;
    private User adminUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John").email("john@example.com")
            .password("hashed").role(User.Role.USER).build();

        adminUser = User.builder().id(99L).name("Admin").email("admin@example.com")
            .password("hashed").role(User.Role.ADMIN).build();

        userResponse = UserResponse.builder().id(1L).name("John")
            .email("john@example.com").role(User.Role.USER).build();
    }

    @Test
    void getAllUsers_returnsPagedResponse() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        var result = userService.getAllUsers(Pageable.unpaged());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getUserById_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        assertThat(userService.getUserById(1L).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_selfUpdate_succeeds() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("John Updated");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, req, "john@example.com");
        assertThat(result).isNotNull();
    }

    @Test
    void updateUser_otherUserAsNonAdmin_throwsForbidden() {
        User other = User.builder().id(2L).email("other@example.com").role(User.Role.USER).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(1L, new UpdateUserRequest(), "other@example.com"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateUser_adminCanUpdateAnyUser() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any())).thenReturn(user);
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        assertThatCode(() -> userService.updateUser(1L, req, "admin@example.com"))
            .doesNotThrowAnyException();
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_found_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertThatCode(() -> userService.deleteUser(1L)).doesNotThrowAnyException();
        verify(userRepository).deleteById(1L);
    }
}
