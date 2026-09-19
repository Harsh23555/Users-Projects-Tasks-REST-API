package com.example.taskmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("TestSecretKeyForJWTSigningThatIsLongEnoughForHS256Algorithm");
        props.setExpirationMs(3_600_000L); // 1 hour
        jwtService = new JwtService(props);
    }

    private UserDetails makeUser(String email) {
        return User.builder()
            .username(email)
            .password("irrelevant")
            .authorities(Collections.emptyList())
            .build();
    }

    @Test
    void generateToken_notNull() {
        String token = jwtService.generateToken(makeUser("test@example.com"));
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_matchesSubject() {
        UserDetails ud = makeUser("alice@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        UserDetails ud = makeUser("bob@example.com");
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.isTokenValid(token, ud)).isTrue();
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(makeUser("alice@example.com"));
        assertThat(jwtService.isTokenValid(token, makeUser("bob@example.com"))).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken(makeUser("alice@example.com"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenValid(tampered, makeUser("alice@example.com"))).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("TestSecretKeyForJWTSigningThatIsLongEnoughForHS256Algorithm");
        expiredProps.setExpirationMs(-1000L); // already expired
        JwtService expiredService = new JwtService(expiredProps);

        String token = expiredService.generateToken(makeUser("carol@example.com"));
        assertThat(expiredService.isTokenValid(token, makeUser("carol@example.com"))).isFalse();
    }
}
