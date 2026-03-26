package com.unishare.api.modules.user.dto;

import lombok.Data;

@Data
public class UserEducationResponse {
    private Long id;
    private Long userId;
    private String university;
    private String major;
    private Integer startYear;
    private Integer endYear;
    private String description;
}
