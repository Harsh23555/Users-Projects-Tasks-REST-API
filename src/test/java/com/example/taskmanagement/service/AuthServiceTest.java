package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.request.LoginRequest;
import com.example.taskmanagement.dto.request.RegisterRequest;
import com.example.taskmanagement.dto.response.AuthResponse;
import com.example.taskmanagement.dto.response.UserResponse;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.exception.DuplicateResourceException;
import com.example.taskmanagement.mapper.EntityMapper;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.security.JwtService;
import com.example.taskmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock EntityMapper mapper;

    @InjectMocks AuthServiceImpl authService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L).name("John").email("john@example.com")
            .password("hashed").role(User.Role.USER).build();

        userResponse = UserResponse.builder()
            .id(1L).name("John").email("john@example.com")
            .role(User.Role.USER).build();
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John"); req.setEmail("john@example.com"); req.setPassword("pass1234");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = authService.register(req);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("john@example.com"); req.setName("J"); req.setPassword("pass1234");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com"); req.setPassword("pass1234");

        UserDetails ud = mock(UserDetails.class);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userDetailsService.loadUserByUsername(req.getEmail())).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn("jwt-token");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(mapper.toUserResponse(user)).thenReturn(userResponse);

        AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUser().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void login_badCredentials_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com"); req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadCredentialsException.class);
    }
}
