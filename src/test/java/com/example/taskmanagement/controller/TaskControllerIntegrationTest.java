package com.example.taskmanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createTask_owner_returns201() throws Exception {
        Map<String, String> body = Map.of("title", "New Task", "priority", "HIGH");

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("New Task"))
            .andExpect(jsonPath("$.data.priority").value("HIGH"));
    }

    @Test
    void createTask_nonOwner_returns403() throws Exception {
        Map<String, String> body = Map.of("title", "Hack Task");

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                .header("Authorization", tokenFor(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createTask_blankTitle_returns400() throws Exception {
        Map<String, String> body = Map.of("title", "");

        mockMvc.perform(post("/api/projects/" + project.getId() + "/tasks")
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void getAllTasks_noFilter_returnsList() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getAllTasks_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/tasks?status=TODO")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getAllTasks_filterByPriority() throws Exception {
        mockMvc.perform(get("/api/tasks?priority=MEDIUM")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getAllTasks_searchByTitle() throws Exception {
        mockMvc.perform(get("/api/tasks?search=Test")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getTaskById_found_returns200() throws Exception {
        mockMvc.perform(get("/api/tasks/" + task.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Test Task"));
    }

    @Test
    void getTaskById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999999")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskStatus_owner_returns200() throws Exception {
        Map<String, String> body = Map.of("status", "IN_PROGRESS");

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/status")
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTaskStatus_nonOwnerNonAssignee_returns403() throws Exception {
        Map<String, String> body = Map.of("status", "COMPLETED");

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/status")
                .header("Authorization", tokenFor(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isForbidden());
    }

    @Test
    void assignTask_owner_returns200() throws Exception {
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assign/" + otherUser.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assignedUser.email").value("jane@example.com"));
    }

    @Test
    void deleteTask_owner_returns200() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + task.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteTask_nonOwner_returns403() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + task.getId())
                .header("Authorization", tokenFor(otherUser)))
            .andExpect(status().isForbidden());
    }

    @Test
    void pagination_works() throws Exception {
        mockMvc.perform(get("/api/tasks?page=0&size=5")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.page").value(0))
            .andExpect(jsonPath("$.data.size").value(5));
    }
}
