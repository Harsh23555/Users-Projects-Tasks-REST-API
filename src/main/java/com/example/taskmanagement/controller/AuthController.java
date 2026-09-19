package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.request.LoginRequest;
import com.example.taskmanagement.dto.request.RegisterRequest;
import com.example.taskmanagement.dto.response.ApiResponse;
import com.example.taskmanagement.dto.response.AuthResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", auth));
    }
}
