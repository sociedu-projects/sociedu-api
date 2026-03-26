package com.unishare.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentAssetRequest {
    @NotBlank
    private String type;
    
    @NotBlank
    private String fileUrl;
    
    private Long fileSize;
}
