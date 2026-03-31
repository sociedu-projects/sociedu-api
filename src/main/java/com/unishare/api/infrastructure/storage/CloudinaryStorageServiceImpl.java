package com.unishare.api.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.dto.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryStorageServiceImpl implements FileStorageService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto" // Hỗ trợ pdf, docx, zip (raw), image
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new RuntimeException("Failed to upload file"); // Replace with specific AppException if needed
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                // Cloudinary thường cần resource_type (image, raw, video). Mặc định gọi xóa image/video, 
                // nhưng nếu xoá tài liệu raw (pdf, zip) sẽ dùng "raw". Ở mức cơ bản dùng "image".
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary: " + fileUrl, e);
            return false;
        }
    }

    private String extractPublicId(String fileUrl) {
        // e.g., https://res.cloudinary.com/dxyz/image/upload/v1234567/folder_name/sample.png -> folder_name/sample
        try {
            String[] parts = fileUrl.split("/");
            String fileWithExt = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            String fileName = fileWithExt.substring(0, fileWithExt.lastIndexOf('.'));
            return folder + "/" + fileName; // Đơn giản hóa, tuỳ thuộc cấu trúc thực tế
        } catch (Exception e) {
            return null;
        }
    }
}
