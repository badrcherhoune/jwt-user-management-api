package com.challenge.users;

import com.challenge.users.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "testSecretKey1234567890testSecretKey1234567890testSecretKey1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtUtil.generateToken("testuser", "test@example.com");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnCorrectSubject() {
        String token = jwtUtil.generateToken("johndoe", "john@example.com");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("johndoe");
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("user1", "user1@example.com");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForRandomString() {
        assertThat(jwtUtil.validateToken("not.a.valid.jwt")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken("user2", "user2@example.com") + "tampered";
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyString() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }
}
