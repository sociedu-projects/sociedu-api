package com.unishare.api.modules.document.dto;

import lombok.Data;

@Data
public class DocumentAssetResponse {
    private Long id;
    private String type;
    private String fileUrl;
    private Long fileSize;
}
