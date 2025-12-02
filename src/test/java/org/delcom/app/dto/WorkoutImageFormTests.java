package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkoutImageFormTests {

    private WorkoutImageForm workoutImageForm;
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setup() {
        workoutImageForm = new WorkoutImageForm();
        mockMultipartFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Constructor default membuat objek kosong")
    void constructor_default_membuat_objek_kosong() {
        // Act
        WorkoutImageForm form = new WorkoutImageForm();

        // Assert
        assertNull(form.getId());
        assertNull(form.getImageFile());
    }

    @Test
    @DisplayName("Setter dan Getter untuk ID bekerja dengan benar")
    void setter_dan_getter_untuk_id_bekerja_dengan_benar() {
        // Arrange
        UUID expectedId = UUID.randomUUID();

        // Act
        workoutImageForm.setId(expectedId);
        UUID actualId = workoutImageForm.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    @DisplayName("Setter dan Getter untuk imageFile bekerja dengan benar")
    void setter_dan_getter_untuk_imageFile_bekerja_dengan_benar() {
        // Act
        workoutImageForm.setImageFile(mockMultipartFile);
        MultipartFile actualFile = workoutImageForm.getImageFile();

        // Assert
        assertEquals(mockMultipartFile, actualFile);
    }

    @Test
    @DisplayName("isEmpty return true ketika imageFile null")
    void isEmpty_return_true_ketika_imageFile_null() {
        // Arrange
        workoutImageForm.setImageFile(null);

        // Act
        boolean result = workoutImageForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEmpty return true ketika imageFile empty")
    void isEmpty_return_true_ketika_imageFile_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEmpty return false ketika imageFile tidak empty")
    void isEmpty_return_false_ketika_imageFile_tidak_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isEmpty();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("getOriginalFilename return null ketika imageFile null")
    void getOriginalFilename_return_null_ketika_imageFile_null() {
        // Arrange
        workoutImageForm.setImageFile(null);

        // Act
        String result = workoutImageForm.getOriginalFilename();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("getOriginalFilename return filename ketika imageFile ada")
    void getOriginalFilename_return_filename_ketika_imageFile_ada() {
        // Arrange
        String expectedFilename = "test-image.jpg";
        when(mockMultipartFile.getOriginalFilename()).thenReturn(expectedFilename);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        String result = workoutImageForm.getOriginalFilename();

        // Assert
        assertEquals(expectedFilename, result);
    }

    @Test
    @DisplayName("isValidImage return false ketika imageFile null")
    void isValidImage_return_false_ketika_imageFile_null() {
        // Arrange
        workoutImageForm.setImageFile(null);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return false ketika imageFile empty")
    void isValidImage_return_false_ketika_imageFile_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return false ketika contentType null")
    void isValidImage_return_false_ketika_contentType_null() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn(null);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/jpeg")
    void isValidImage_return_true_untuk_image_jpeg() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/png")
    void isValidImage_return_true_untuk_image_png() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/gif")
    void isValidImage_return_true_untuk_image_gif() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/webp")
    void isValidImage_return_true_untuk_image_webp() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return false untuk content type non-image")
    void isValidImage_return_false_untuk_content_type_non_image() {
        // Arrange
        String[] invalidContentTypes = {
                "text/plain",
                "application/pdf",
                "application/octet-stream",
                "video/mp4",
                "audio/mpeg",
                "image/svg+xml", // SVG tidak didukung
                "image/bmp" // BMP tidak didukung
        };

        for (String contentType : invalidContentTypes) {
            when(mockMultipartFile.isEmpty()).thenReturn(false);
            when(mockMultipartFile.getContentType()).thenReturn(contentType);
            workoutImageForm.setImageFile(mockMultipartFile);

            // Act
            boolean result = workoutImageForm.isValidImage();

            // Assert
            assertFalse(result, "Should return false for content type: " + contentType);
        }
    }

    @Test
    @DisplayName("isSizeValid return false ketika imageFile null")
    void isSizeValid_return_false_ketika_imageFile_null() {
        // Arrange
        workoutImageForm.setImageFile(null);
        long maxSize = 1024 * 1024; // 1MB

        // Act
        boolean result = workoutImageForm.isSizeValid(maxSize);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isSizeValid return true ketika file size sama dengan maxSize")
    void isSizeValid_return_true_ketika_file_size_sama_dengan_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        when(mockMultipartFile.getSize()).thenReturn(maxSize);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isSizeValid(maxSize);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isSizeValid return true ketika file size kurang dari maxSize")
    void isSizeValid_return_true_ketika_file_size_kurang_dari_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        long fileSize = 512 * 1024; // 0.5MB
        when(mockMultipartFile.getSize()).thenReturn(fileSize);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isSizeValid(maxSize);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isSizeValid return false ketika file size lebih dari maxSize")
    void isSizeValid_return_false_ketika_file_size_lebih_dari_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        long fileSize = 2 * 1024 * 1024; // 2MB
        when(mockMultipartFile.getSize()).thenReturn(fileSize);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isSizeValid(maxSize);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isSizeValid return true untuk file size 0 dengan maxSize 0")
    void isSizeValid_return_true_untuk_file_size_0_dengan_maxSize_0() {
        // Arrange
        when(mockMultipartFile.getSize()).thenReturn(0L);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Act
        boolean result = workoutImageForm.isSizeValid(0L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Integration test - form valid untuk image JPEG ukuran normal")
    void integration_test_form_valid_untuk_image_JPEG_ukuran_normal() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getSize()).thenReturn(500 * 1024L); // 500KB
        when(mockMultipartFile.getOriginalFilename()).thenReturn("photo.jpg");

        workoutImageForm.setId(id);
        workoutImageForm.setImageFile(mockMultipartFile);

        // Assert semua kondisi
        assertFalse(workoutImageForm.isEmpty());
        assertEquals("photo.jpg", workoutImageForm.getOriginalFilename());
        assertTrue(workoutImageForm.isValidImage());
        assertTrue(workoutImageForm.isSizeValid(1024 * 1024)); // 1MB max
        assertEquals(id, workoutImageForm.getId());
    }

    @Test
    @DisplayName("Integration test - form invalid untuk file besar")
    void integration_test_form_invalid_untuk_file_besar() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        when(mockMultipartFile.getSize()).thenReturn(5 * 1024 * 1024L); // 5MB
        when(mockMultipartFile.getOriginalFilename()).thenReturn("large-image.png");

        workoutImageForm.setImageFile(mockMultipartFile);

        // Assert
        assertFalse(workoutImageForm.isEmpty());
        assertTrue(workoutImageForm.isValidImage()); // Masih valid sebagai image
        assertFalse(workoutImageForm.isSizeValid(2 * 1024 * 1024)); // Tapi size melebihi 2MB
    }

    @Test
    @DisplayName("Edge case - contentType case insensitive")
    void edge_case_contentType_case_insensitive() {
        // Arrange
        String[] caseVariations = {
                "IMAGE/JPEG",
                "Image/Jpeg",
                "image/JPEG",
                "IMAGE/jpeg"
        };

        for (String contentType : caseVariations) {
            when(mockMultipartFile.isEmpty()).thenReturn(false);
            when(mockMultipartFile.getContentType()).thenReturn(contentType);
            workoutImageForm.setImageFile(mockMultipartFile);

            // Act
            boolean result = workoutImageForm.isValidImage();

            // Assert
            assertFalse(result, "Should return false for case variation: " + contentType);
        }
    }
}