package com.example.taskmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
