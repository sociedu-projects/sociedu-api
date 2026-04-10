package com.unishare.api.modules.profile.dto;

import com.unishare.api.modules.user.dto.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

public class ProfileDto {

    // ======================== GET /profile/me ========================

    @Data
    @Builder
    public static class MyProfileResponse {
        private Long userId;
        private String fullName;
        private String avatarUrl;
        private String bio;
        private Instant createdAt;
        private Instant updatedAt;
        private List<UserEducationResponse> educations;
        private List<UserLanguageResponse> languages;
        private List<UserExperienceResponse> experiences;
        private List<UserCertificateResponse> certificates;
    }

    // ======================== PATCH /profile/me ========================

    @Data
    public static class UpdateProfileRequest {
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        private String fullName;

        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        private String bio;
    }

    // ======================== POST /profile/avatar ========================

    @Data
    @Builder
    public static class AvatarUploadResponse {
        private String avatarUrl;
    }
}
