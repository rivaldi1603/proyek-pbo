package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class LoginFormTests {

    private LoginForm loginForm;
    private Validator validator;

    @BeforeEach
    void setUp() {
        loginForm = new LoginForm();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(loginForm.getEmail());
        assertNull(loginForm.getPassword());
        assertFalse(loginForm.isRememberMe());
    }

    @Test
    @DisplayName("Setter dan Getter untuk email bekerja dengan benar")
    void setterAndGetter_Email_WorksCorrectly() {
        String email = "test@example.com";
        loginForm.setEmail(email);
        assertEquals(email, loginForm.getEmail());
    }

    @Test
    @DisplayName("Setter dan Getter untuk password bekerja dengan benar")
    void setterAndGetter_Password_WorksCorrectly() {
        String password = "password123";
        loginForm.setPassword(password);
        assertEquals(password, loginForm.getPassword());
    }

    @Test
    @DisplayName("Setter dan Getter untuk rememberMe bekerja dengan benar")
    void setterAndGetter_RememberMe_WorksCorrectly() {
        loginForm.setRememberMe(true);
        assertTrue(loginForm.isRememberMe());

        loginForm.setRememberMe(false);
        assertFalse(loginForm.isRememberMe());
    }

    @Test
    @DisplayName("Validation berhasil ketika semua field valid")
    void validation_Success_WhenAllFieldsValid() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation gagal ketika email null")
    void validation_Fail_WhenEmailIsNull() {
        loginForm.setEmail(null);
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika email empty string")
    void validation_Fail_WhenEmailIsEmpty() {
        loginForm.setEmail("");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika format email tidak valid")
    void validation_Fail_WhenEmailFormatInvalid() {
        loginForm.setEmail("invalid-email");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password null")
    void validation_Fail_WhenPasswordIsNull() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword(null);

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password empty string")
    void validation_Fail_WhenPasswordIsEmpty() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }
}