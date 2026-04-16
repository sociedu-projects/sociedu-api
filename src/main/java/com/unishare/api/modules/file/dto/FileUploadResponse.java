package com.unishare.api.modules.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FileUploadResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private String visibility;
    private Instant createdAt;
}
