package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${app.upload.dir:./uploads}")
    protected String uploadDir;

    public String storeFile(MultipartFile file, UUID todoId) throws IOException {
        // Buat directory jika belum ada
        // Buat directory jika belum ada
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        System.out.println("DEBUG: Storing file to " + uploadPath.toAbsolutePath());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.equals("image/jpeg"))
                    fileExtension = ".jpg";
                else if (contentType.equals("image/png"))
                    fileExtension = ".png";
                else if (contentType.equals("image/webp"))
                    fileExtension = ".webp";
                else if (contentType.equals("image/gif"))
                    fileExtension = ".gif";
            }
        }

        String filename = "cover_" + todoId.toString() + fileExtension;

        // Simpan file
        Path filePath = uploadPath.resolve(filename);
        try (java.io.InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return filename;
    }

    public String storeProfilePhoto(MultipartFile file, UUID userId) throws IOException {
        // Buat directory jika belum ada
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.equals("image/jpeg"))
                    fileExtension = ".jpg";
                else if (contentType.equals("image/png"))
                    fileExtension = ".png";
                else if (contentType.equals("image/webp"))
                    fileExtension = ".webp";
                else if (contentType.equals("image/gif"))
                    fileExtension = ".gif";
            }
        }

        String filename = "profile_" + userId.toString() + fileExtension;

        // Simpan file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename).normalize();
    }

    public boolean fileExists(String filename) {
        return Files.exists(loadFile(filename));
    }
}
