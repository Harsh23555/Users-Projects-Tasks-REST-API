package com.example.taskmanagement.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DashboardResponse {
    private long totalUsers;
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
}
