package com.unishare.api.modules.products.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class DocumentResponse {
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private String subject;
    private String university;
    private String major;
    private String docType;
    private BigDecimal price;
    private String status;
    private Double ratingAvg;
    private Integer salesCount;
    private Instant createdAt;
    private Instant updatedAt;
    
    private List<DocumentAssetResponse> assets;
}
