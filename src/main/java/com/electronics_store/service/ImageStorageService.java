package com.electronics_store.service;

import com.electronics_store.dto.image.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    ImageUploadResult upload(MultipartFile file);

    void delete(String publicId);
}