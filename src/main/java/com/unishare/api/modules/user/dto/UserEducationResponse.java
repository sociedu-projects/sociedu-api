package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserEducationResponse {
    private UUID id;
    private UUID userId;
    private UUID universityId;
    private UUID majorId;
    /** Tên trường (join, có thể null nếu chưa resolve). */
    private String universityName;
    /** Tên ngành (join). */
    private String majorName;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
}
