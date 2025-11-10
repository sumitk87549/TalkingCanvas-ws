package com.example.talkingCanvas.util;

import com.example.talkingCanvas.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for file storage operations
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new BadRequestException("Invalid file type. Only images are allowed");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            // Create subdirectory if it doesn't exist
            Path targetLocation = this.fileStorageLocation.resolve(subdirectory);
            Files.createDirectories(targetLocation);

            // Copy file to target location
            Path filePath = targetLocation.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {}", filename);
            return subdirectory + "/" + filename;
        } catch (IOException ex) {
            logger.error("Failed to store file", ex);
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path fileToDelete = this.fileStorageLocation.resolve(filePath).normalize();
            Files.deleteIfExists(fileToDelete);
            logger.info("File deleted successfully: {}", filePath);
        } catch (IOException ex) {
            logger.error("Failed to delete file: {}", filePath, ex);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/gif");
    }
}
