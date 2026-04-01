package com.unishare.api.modules.products.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentCategoryRequest {
    @NotBlank
    private String name;
    
    private Long parentId;
}
