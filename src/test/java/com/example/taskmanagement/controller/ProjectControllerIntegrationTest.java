package com.example.taskmanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createProject_authenticated_returns201() throws Exception {
        Map<String, String> body = Map.of("name", "New Project", "description", "Desc");

        mockMvc.perform(post("/api/projects")
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("New Project"));
    }

    @Test
    void createProject_unauthenticated_returns401() throws Exception {
        Map<String, String> body = Map.of("name", "Proj");

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_blankName_returns400() throws Exception {
        Map<String, String> body = Map.of("name", "");

        mockMvc.perform(post("/api/projects")
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getAllProjects_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/projects")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getAllProjects_filterByStatus_returnsFiltered() throws Exception {
        mockMvc.perform(get("/api/projects?status=ACTIVE")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getProjectById_found_returns200() throws Exception {
        mockMvc.perform(get("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    void getProjectById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/projects/999999")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateProject_owner_returns200() throws Exception {
        Map<String, String> body = Map.of("name", "Updated Project");

        mockMvc.perform(put("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Updated Project"));
    }

    @Test
    void updateProject_nonOwner_returns403() throws Exception {
        Map<String, String> body = Map.of("name", "Hijacked");

        mockMvc.perform(put("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateProject_admin_returns200() throws Exception {
        Map<String, String> body = Map.of("name", "Admin Updated");

        mockMvc.perform(put("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isOk());
    }

    @Test
    void deleteProject_owner_returns200() throws Exception {
        mockMvc.perform(delete("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteProject_nonOwner_returns403() throws Exception {
        mockMvc.perform(delete("/api/projects/" + project.getId())
                .header("Authorization", tokenFor(otherUser)))
            .andExpect(status().isForbidden());
    }
}
