package com.unishare.api.modules.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentCategoryRequest {
    @NotBlank
    private String name;
    
    private Long parentId;
}
