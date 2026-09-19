package com.example.taskmanagement.service.impl;

import com.example.taskmanagement.dto.request.UpdateUserRequest;
import com.example.taskmanagement.dto.response.PagedResponse;
import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.DuplicateResourceException;
import com.example.taskmanagement.exception.ForbiddenException;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;

    @Override
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable).map(mapper::toUserResponse);
        return PagedResponse.from(page);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapper.toUserResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentUserEmail) {
        User user = findUser(id);
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", null));

        // Only ADMIN or the user themselves can update
        if (currentUser.getRole() != User.Role.ADMIN && !currentUser.getId().equals(id)) {
            throw new ForbiddenException("You can only update your own profile");
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<ProjectResponse> getUserProjects(Long id) {
        User user = findUser(id);
        return projectRepository.findByOwner(user).stream()
            .map(mapper::toProjectResponse)
            .toList();
    }

    @Override
    public List<TaskResponse> getUserTasks(Long id) {
        User user = findUser(id);
        return taskRepository.findByAssignedUser(user).stream()
            .map(mapper::toTaskResponse)
            .toList();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
