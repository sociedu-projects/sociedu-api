package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserEducationRequest {
    private UUID universityId;
    private UUID majorId;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
}
