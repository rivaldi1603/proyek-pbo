package org.delcom.app.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordForm {

    @NotBlank(message = "Password lama harus diisi")
    private String oldPassword;

    @NotBlank(message = "Password baru harus diisi")
    private String newPassword;

    @NotBlank(message = "Konfirmasi password baru harus diisi")
    private String confirmPassword;

    public ChangePasswordForm() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
