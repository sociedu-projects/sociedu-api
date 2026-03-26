package com.unishare.api.modules.document.dto;

import lombok.Data;

@Data
public class DocumentCategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
}
