package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserExperienceResponse {
    private UUID id;
    private UUID userId;
    private String company;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
}
