package com.unishare.api.modules.products.dto;

import lombok.Data;

@Data
public class DocumentCategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
}
