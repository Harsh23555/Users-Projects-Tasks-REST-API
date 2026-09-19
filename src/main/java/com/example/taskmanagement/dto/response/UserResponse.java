package com.example.taskmanagement.dto.response;

import com.example.taskmanagement.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // password is intentionally omitted
}
