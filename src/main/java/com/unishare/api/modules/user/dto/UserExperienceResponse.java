package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserExperienceResponse {
    private Long id;
    private Long userId;
    private String company;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
