package com.electronics_store.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.electronics_store.dto.image.ImageUploadResult;
import com.electronics_store.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageService implements ImageStorageService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public ImageUploadResult upload(MultipartFile file) {
        validate(file);

        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "products",
                            "resource_type", "image"
                    )
            );

            return new ImageUploadResult(
                    result.get("secure_url").toString(),
                    result.get("public_id").toString()
            );

        } catch (IOException e) {
            throw new RuntimeException("Upload to Cloudinary failed", e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException("Delete image failed", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty())
            throw new RuntimeException("File is empty");

        if (!file.getContentType().startsWith("image/"))
            throw new RuntimeException("File must be image");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("Max image size is 5MB");
    }
}
