package com.example.taskmanagement.mapper;

import com.example.taskmanagement.dto.response.ProjectResponse;
import com.example.taskmanagement.dto.response.TaskResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.Project;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) return null;
        return ProjectResponse.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .status(project.getStatus())
            .owner(toUserResponse(project.getOwner()))
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
            .build();
    }

    public TaskResponse toTaskResponse(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .dueDate(task.getDueDate())
            .projectId(task.getProject() != null ? task.getProject().getId() : null)
            .projectName(task.getProject() != null ? task.getProject().getName() : null)
            .assignedUser(toUserResponse(task.getAssignedUser()))
            .overdue(task.isOverdue())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
