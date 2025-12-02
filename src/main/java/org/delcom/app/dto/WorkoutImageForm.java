package org.delcom.app.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public class WorkoutImageForm {

    private UUID id;

    @NotNull(message = "Image tidak boleh kosong")
    private MultipartFile imageFile;

    // Constructor
    public WorkoutImageForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    // Helper methods
    public boolean isEmpty() {
        return imageFile == null || imageFile.isEmpty();
    }

    public String getOriginalFilename() {
        return imageFile != null ? imageFile.getOriginalFilename() : null;
    }

    // Validation methods
    public boolean isValidImage() {
        if (this.isEmpty()) {
            return false;
        }

        String contentType = imageFile.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    public boolean isSizeValid(long maxSize) {
        return imageFile != null && imageFile.getSize() <= maxSize;
    }
}