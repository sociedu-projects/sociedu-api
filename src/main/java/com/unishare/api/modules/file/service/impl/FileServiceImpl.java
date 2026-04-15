package com.unishare.api.modules.file.service.impl;

import com.unishare.api.common.constants.FileVisibility;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.storage.FileStorageService;
import com.unishare.api.modules.file.dto.FileUploadResponse;
import com.unishare.api.modules.file.entity.StoredFile;
import com.unishare.api.modules.file.exception.FileErrorCode;
import com.unishare.api.modules.file.repository.StoredFileRepository;
import com.unishare.api.modules.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StoredFileRepository storedFileRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public FileUploadResponse upload(Long uploaderId, MultipartFile file, String folder, String visibility,
                                     String entityType, Long entityId) {
        String url = fileStorageService.uploadFile(file, folder != null ? folder : "uploads");
        StoredFile sf = new StoredFile();
        sf.setUploaderId(uploaderId);
        sf.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        sf.setFileUrl(url);
        sf.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        sf.setFileSize(file.getSize());
        sf.setVisibility(visibility != null ? visibility : FileVisibility.PRIVATE);
        sf.setEntityType(entityType);
        sf.setEntityId(entityId);
        sf = storedFileRepository.save(sf);
        return toResponse(sf);
    }

    @Override
    @Transactional(readOnly = true)
    public FileUploadResponse getFile(Long fileId, Long requesterUserId) {
        StoredFile sf = storedFileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new AppException(FileErrorCode.FILE_NOT_FOUND));
        if (!FileVisibility.PUBLIC.equalsIgnoreCase(sf.getVisibility())
                && (sf.getUploaderId() == null || !sf.getUploaderId().equals(requesterUserId))) {
            throw new AppException(FileErrorCode.FILE_ACCESS_DENIED);
        }
        return toResponse(sf);
    }

    @Override
    @Transactional
    public void softDelete(Long fileId, Long requesterUserId) {
        StoredFile sf = storedFileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new AppException(FileErrorCode.FILE_NOT_FOUND));
        if (sf.getUploaderId() == null || !sf.getUploaderId().equals(requesterUserId)) {
            throw new AppException(FileErrorCode.FILE_ACCESS_DENIED);
        }
        sf.setDeletedAt(java.time.Instant.now());
        storedFileRepository.save(sf);
    }

    private static FileUploadResponse toResponse(StoredFile sf) {
        return FileUploadResponse.builder()
                .id(sf.getId())
                .fileName(sf.getFileName())
                .fileUrl(sf.getFileUrl())
                .mimeType(sf.getMimeType())
                .fileSize(sf.getFileSize())
                .visibility(sf.getVisibility())
                .createdAt(sf.getCreatedAt())
                .build();
    }
}
