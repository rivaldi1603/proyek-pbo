package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTests {

    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile mockMultipartFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        fileStorageService = new FileStorageService();
        fileStorageService.uploadDir = tempDir.toString();
    }

    @Test
    @DisplayName("storeFile should save file with correct name")
    void storeFile_Success() throws Exception {
        UUID id = UUID.randomUUID();
        String originalFilename = "image.jpg";
        String expectedFilename = "cover_" + id + ".jpg";
        byte[] content = "fake image".getBytes();

        when(mockMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        String result = fileStorageService.storeFile(mockMultipartFile, id);

        assertEquals(expectedFilename, result);
        Path storedFile = tempDir.resolve(expectedFilename);
        assertTrue(Files.exists(storedFile));
    }

    @Test
    @DisplayName("storeFile should work with null filename (default extension)")
    void storeFile_NullFilename() throws Exception {
        UUID id = UUID.randomUUID();
        String expectedFilename = "cover_" + id; // Logic currently doesn't add extension if null

        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        assertEquals(expectedFilename, result);
    }

    @Test
    @DisplayName("storeFile should throw IOException on failure")
    void storeFile_IOException() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Simulated"));

        assertThrows(IOException.class, () -> fileStorageService.storeFile(mockMultipartFile, id));
    }

    @Test
    @DisplayName("deleteFile should return true if deleted")
    void deleteFile_Success() throws Exception {
        String filename = "test.txt";
        Path file = tempDir.resolve(filename);
        Files.write(file, "content".getBytes());
        assertTrue(Files.exists(file));

        boolean result = fileStorageService.deleteFile(filename);

        assertTrue(result);
        assertFalse(Files.exists(file));
    }

    @Test
    @DisplayName("deleteFile should return false if not exists")
    void deleteFile_NotExists() {
        boolean result = fileStorageService.deleteFile("ghost.txt");
        assertFalse(result);
    }

    @Test
    @DisplayName("deleteFile should return false on IOException")
    void deleteFile_IOException() {
        String filename = "test.txt";

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Simulated"));

            boolean result = fileStorageService.deleteFile(filename);
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("loadFile should return correct path")
    void loadFile() {
        Path result = fileStorageService.loadFile("file.txt");
        assertEquals(tempDir.resolve("file.txt"), result);
    }

    @Test
    @DisplayName("fileExists should check existence")
    void fileExists() throws Exception {
        String filename = "exist.txt";
        Files.write(tempDir.resolve(filename), "data".getBytes());

        assertTrue(fileStorageService.fileExists(filename));
        assertFalse(fileStorageService.fileExists("nope.txt"));

    }

    @Test
    @DisplayName("storeProfilePhoto should save file with correct name")
    void storeProfilePhoto_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        String originalFilename = "me.png";
        String expectedFilename = "profile_" + userId + ".png";
        byte[] content = "fake image".getBytes();

        when(mockMultipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);

        assertEquals(expectedFilename, result);
        Path storedFile = tempDir.resolve(expectedFilename);
        assertTrue(Files.exists(storedFile));
    }

    @Test
    @DisplayName("storeFile should derive extension from content type")
    void storeFile_ContentTypeExtensions() throws Exception {
        UUID id = UUID.randomUUID();

        // JPEG
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob"); // No extension
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        String result = fileStorageService.storeFile(mockMultipartFile, id);
        assertTrue(result.endsWith(".jpg"));

        // PNG
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        result = fileStorageService.storeFile(mockMultipartFile, id);
        assertTrue(result.endsWith(".png"));

        // WEBP
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        result = fileStorageService.storeFile(mockMultipartFile, id);
        assertTrue(result.endsWith(".webp"));

        // GIF
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        result = fileStorageService.storeFile(mockMultipartFile, id);
        assertTrue(result.endsWith(".gif"));
    }

    @Test
    @DisplayName("storeProfilePhoto should derive extension from content type")
    void storeProfilePhoto_ContentTypeExtensions() throws Exception {
        UUID id = UUID.randomUUID();

        // JPEG
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, id);
        assertTrue(result.endsWith(".jpg"));
        assertTrue(result.startsWith("profile_"));
    }

    @Test
    @DisplayName("storeFile with unknown content type (no extension)")
    void storeFile_UnknownContentType() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("unknown/type");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        // Should have no extension or default? Code: extension = "".
        assertEquals("cover_" + id, result);
    }

    @Test
    @DisplayName("storeFile with null content type")
    void storeFile_NullContentType() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        assertEquals("cover_" + id, result);
    }

    @Test
    @DisplayName("storeProfilePhoto with unknown content type")
    void storeProfilePhoto_UnknownContentType() throws Exception {
        UUID userId = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("unknown/type");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertEquals("profile_" + userId, result);
    }

    @Test
    @DisplayName("storeFile when directory already exists")
    void storeFile_DirectoryExists() throws Exception {
        UUID id = UUID.randomUUID();
        // Create dir first
        Files.createDirectories(tempDir);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        assertEquals("cover_" + id + ".jpg", result);
    }

    @Test
    @DisplayName("storeProfilePhoto with null original filename")
    void storeProfilePhoto_NullFilename() throws Exception {
        UUID userId = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertEquals("profile_" + userId, result);
    }

    @Test
    @DisplayName("storeProfilePhoto with null content type")
    void storeProfilePhoto_NullContentType() throws Exception {
        UUID userId = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertEquals("profile_" + userId, result);
    }

    @Test
    @DisplayName("storeProfilePhoto with various content types")
    void storeProfilePhoto_ContentTypes() throws Exception {
        UUID userId = UUID.randomUUID();

        // PNG
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertTrue(result.endsWith(".png"));

        // WEBP
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertTrue(result.endsWith(".webp"));

        // GIF
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertTrue(result.endsWith(".gif"));
    }

    @Test
    @DisplayName("storeProfilePhoto with JPEG content type (no extension)")
    void storeProfilePhoto_JPEG() throws Exception {
        UUID userId = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("blob");
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    @DisplayName("storeProfilePhoto when directory already exists")
    void storeProfilePhoto_DirectoryExists() throws Exception {
        UUID userId = UUID.randomUUID();
        Files.createDirectories(tempDir);

        when(mockMultipartFile.getOriginalFilename()).thenReturn("me.png");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);
        assertEquals("profile_" + userId + ".png", result);
    }

    @Test
    @DisplayName("storeFile with null InputStream")
    void storeFile_NullInputStream() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockMultipartFile.getInputStream()).thenReturn(null);

        // Expect NPE or similar, but the checking of close() on null resource is the
        // goal.
        // Files.copy(null, ...) throws NPE
        assertThrows(NullPointerException.class, () -> fileStorageService.storeFile(mockMultipartFile, id));
    }

    @Test
    @DisplayName("storeFile creates directory if it does not exist")
    void storeFile_CreatesDirectory() throws Exception {
        UUID id = UUID.randomUUID();
        // Point to a non-existent subdirectory
        Path newDir = tempDir.resolve("new_uploads");
        fileStorageService.uploadDir = newDir.toString();

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);

        assertEquals("cover_" + id + ".jpg", result);
        assertTrue(Files.exists(newDir));
    }

    @Test
    @DisplayName("storeProfilePhoto creates directory if it does not exist")
    void storeProfilePhoto_CreatesDirectory() throws Exception {
        UUID userId = UUID.randomUUID();
        // Point to a non-existent subdirectory
        Path newDir = tempDir.resolve("profile_uploads");
        fileStorageService.uploadDir = newDir.toString();

        when(mockMultipartFile.getOriginalFilename()).thenReturn("me.png");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeProfilePhoto(mockMultipartFile, userId);

        assertEquals("profile_" + userId + ".png", result);
        assertTrue(Files.exists(newDir));
    }
}