package com.unishare.api.modules.file.service;

import com.unishare.api.modules.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse upload(Long uploaderId, MultipartFile file, String folder, String visibility,
                              String entityType, Long entityId);

    FileUploadResponse getFile(Long fileId, Long requesterUserId);

    void softDelete(Long fileId, Long requesterUserId);
}
