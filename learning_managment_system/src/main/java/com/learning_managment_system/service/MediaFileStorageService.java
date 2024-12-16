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

    private final String storageLocation = "media_files"; // مجلد تخزين الملفات

    public MediaFileStorageService() throws IOException {
        Path storagePath = Paths.get(storageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
    }

    // ميثود لتحميل ملف الوسائط
    public String uploadFile(MultipartFile file) throws IOException {
        // التحقق من أن الملف ليس فارغًا
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // إنشاء اسم فريد للملف لتجنب التكرار
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(storageLocation, fileName);

        // حفظ الملف في المسار المحدد
        Files.copy(file.getInputStream(), filePath);

        // إعادة مسار الملف لحفظه في قاعدة البيانات
        return filePath.toString();
    }

    // ميثود لاسترجاع ملف من المسار
    public byte[] getFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found");
        }
        return Files.readAllBytes(path);
    }

    // ميثود لحذف ملف
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }
}
