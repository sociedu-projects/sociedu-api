package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserExperienceRequest {
    @NotBlank
    private String company;

    @NotBlank
    private String position;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;
    private Boolean isCurrent;
    private String description;
}
