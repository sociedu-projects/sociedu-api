package com.unishare.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentAssetRequest {
    @NotBlank
    private String fileType;
    
    private String fileFormat;
    
    @NotBlank
    private String fileUrl;
    
    @NotNull
    private Long fileSize;

    private Integer sortOrder;
}
