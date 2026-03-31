package com.unishare.api.modules.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserFullProfileResponse {
    private UserProfileResponse profile;
    private List<UserEducationResponse> educations;
    private List<UserLanguageResponse> languages;
    private List<UserExperienceResponse> experiences;
    private List<UserCertificateResponse> certificates;
}
