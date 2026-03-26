package com.unishare.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DocumentRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String university;
    
    private String major;
    
    @NotBlank
    private String docType;
    
    @NotNull
    private BigDecimal price;
    
    private String status;
    
    private List<DocumentAssetRequest> assets;
}
