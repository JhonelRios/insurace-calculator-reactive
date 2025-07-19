package com.jhonelrios.insurance_quote;

import com.jhonelrios.insurance_quote.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {
    private JwtUtil jwtUtil;

    private static final String SECRET = "super-secret-key-for-testing-jwt-123";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String username = "admin";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);

        String subject = jwtUtil.validateToken(token);
        assertEquals(username, subject);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid-token";

        var exception = assertThrows(ResponseStatusException.class, () -> {
            jwtUtil.validateToken(invalidToken);
        });

        assertEquals(401, exception.getStatusCode().value());
    }
}
