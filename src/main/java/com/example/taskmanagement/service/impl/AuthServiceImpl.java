package com.example.taskmanagement.service.impl;

import com.example.taskmanagement.dto.request.LoginRequest;
import com.example.taskmanagement.dto.request.RegisterRequest;
import com.example.taskmanagement.dto.response.AuthResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.DuplicateResourceException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtService;
import com.example.taskmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EntityMapper mapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(User.Role.USER)
            .build();

        return mapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        return AuthResponse.builder()
            .token(token)
            .user(mapper.toUserResponse(user))
            .build();
    }
}
