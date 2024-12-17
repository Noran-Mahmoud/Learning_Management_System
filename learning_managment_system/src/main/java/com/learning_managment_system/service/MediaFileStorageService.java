package com.learning_managment_system.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class MediaFileStorageService {

    private final String storageLocation = "media_files"; 

    public MediaFileStorageService() throws IOException {
        Path storagePath = Paths.get(storageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
    }


    public String uploadFile(MultipartFile file) throws IOException {
    
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(storageLocation, fileName);

    
        Files.copy(file.getInputStream(), filePath);

        
        return filePath.toString();
    }

    
    public byte[] getFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found");
        }
        return Files.readAllBytes(path);
    }

    
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }
}
