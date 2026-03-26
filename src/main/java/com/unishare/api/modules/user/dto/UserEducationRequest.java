package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserEducationRequest {
    @NotBlank
    private String university;
    
    @NotBlank
    private String major;
    
    @NotNull
    private Integer startYear;
    
    private Integer endYear;
    private String description;
}
