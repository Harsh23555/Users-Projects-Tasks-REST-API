package com.example.taskmanagement.service.impl;

import com.example.taskmanagement.dto.response.DashboardResponse;
import com.example.taskmanagement.repository.ProjectRepository;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public DashboardResponse getSummary() {
        return DashboardResponse.builder()
            .totalUsers(userRepository.count())
            .totalProjects(projectRepository.count())
            .activeProjects(projectRepository.countActiveProjects())
            .totalTasks(taskRepository.count())
            .completedTasks(taskRepository.countCompletedTasks())
            .pendingTasks(taskRepository.countPendingTasks())
            .overdueTasks(taskRepository.countOverdueTasks(LocalDateTime.now()))
            .build();
    }
}
