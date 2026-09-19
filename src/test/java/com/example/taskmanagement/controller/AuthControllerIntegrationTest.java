package com.example.taskmanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void register_success_returns201() throws Exception {
        Map<String, String> body = Map.of(
            "name", "New User",
            "email", "newuser@example.com",
            "password", "secure123"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        Map<String, String> body = Map.of(
            "name", "John Again",
            "email", "john@example.com",
            "password", "secure123"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        Map<String, String> body = Map.of(
            "name", "X", "email", "not-an-email", "password", "secure123"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        Map<String, String> body = Map.of(
            "name", "John", "email", "x@example.com", "password", "short"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_success_returnsToken() throws Exception {
        Map<String, String> body = Map.of(
            "email", "john@example.com",
            "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.user.email").value("john@example.com"))
            .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        Map<String, String> body = Map.of(
            "email", "john@example.com",
            "password", "wrongpass"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        Map<String, String> body = Map.of(
            "email", "ghost@example.com",
            "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)))
            .andExpect(status().isUnauthorized());
    }
}
