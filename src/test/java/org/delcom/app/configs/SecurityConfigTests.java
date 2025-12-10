package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("Beans should be loaded")
    void contextLoads() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("PasswordEncoder should be valid BCrypt")
    void testPasswordEncoder() {
        String raw = "password";
        String encoded = passwordEncoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }
}
