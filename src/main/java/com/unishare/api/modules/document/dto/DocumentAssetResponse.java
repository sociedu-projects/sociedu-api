package com.unishare.api.modules.document.dto;

import lombok.Data;

@Data
public class DocumentAssetResponse {
    private Long id;
    private String fileType;
    private String fileFormat;
    private String fileUrl;
    private Long fileSize;
    private Integer sortOrder;
}
