package com.unishare.api.modules.document.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class DocumentReviewResponse {
    private Long id;
    private Long userId;
    private Long documentId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
