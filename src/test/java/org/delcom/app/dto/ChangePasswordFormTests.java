package org.delcom.app.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangePasswordFormTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Default Constructor and Setters/Getters")
    void testSettersGetters() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("oldPass");
        form.setNewPassword("newPass");
        form.setConfirmPassword("newPass");

        assertEquals("oldPass", form.getOldPassword());
        assertEquals("newPass", form.getNewPassword());
        assertEquals("newPass", form.getConfirmPassword());
    }

    @Test
    @DisplayName("Validation: Success")
    void testValidation_Success() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setOldPassword("oldPass");
        form.setNewPassword("newPass");
        form.setConfirmPassword("newPass");

        var violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation: Fail empty fields")
    void testValidation_Fail() {
        ChangePasswordForm form = new ChangePasswordForm();

        var violations = validator.validate(form);
        assertEquals(3, violations.size()); // All 3 fields are @NotBlank
    }
}
