package com.unishare.api.modules.file.service;

import com.unishare.api.modules.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {

    FileUploadResponse upload(UUID uploaderId, MultipartFile file, String folder, String visibility,
                              String entityType, UUID entityId);

    FileUploadResponse getFile(UUID fileId, UUID requesterUserId);

    void softDelete(UUID fileId, UUID requesterUserId);
}
