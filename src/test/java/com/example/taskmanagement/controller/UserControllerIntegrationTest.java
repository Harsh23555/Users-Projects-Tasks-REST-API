package com.example.taskmanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllUsers_adminCanAccess() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", tokenFor(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getAllUsers_regularUserForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_self_returns200() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("john@example.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/999999")
                .header("Authorization", tokenFor(adminUser)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_self_returns200() throws Exception {
        Map<String, String> body = Map.of("name", "John Updated");

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", tokenFor(regularUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void updateUser_otherUserForbidden() throws Exception {
        Map<String, String> body = Map.of("name", "Hacked");

        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", tokenFor(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_admin_returns200() throws Exception {
        mockMvc.perform(delete("/api/users/" + otherUser.getId())
                .header("Authorization", tokenFor(adminUser)))
            .andExpect(status().isOk());
    }

    @Test
    void deleteUser_regularUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/" + otherUser.getId())
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isForbidden());
    }

    @Test
    void getUserProjects_returns200() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId() + "/projects")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUserTasks_returns200() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId() + "/tasks")
                .header("Authorization", tokenFor(regularUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }
}
