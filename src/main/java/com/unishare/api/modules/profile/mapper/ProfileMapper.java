package com.unishare.api.modules.profile.mapper;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplicationResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationStatusResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.MyProfileResponse;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper cho module profile.
 * Chuyển đổi Entity → DTO, tuân theo pattern của UserMapper.
 */
@Component
public class ProfileMapper {

    /**
     * Gộp UserProfile + các sub-collections thành MyProfileResponse.
     */
    public MyProfileResponse toMyProfileResponse(
            UserProfile profile,
            List<UserEducationResponse> educations,
            List<UserLanguageResponse> languages,
            List<UserExperienceResponse> experiences,
            List<UserCertificateResponse> certificates) {

        return MyProfileResponse.builder()
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .educations(educations)
                .languages(languages)
                .experiences(experiences)
                .certificates(certificates)
                .build();
    }

    /**
     * Map MentorProfile entity → MentorApplicationResponse.
     */
    public MentorApplicationResponse toMentorApplicationResponse(MentorProfile entity) {
        return MentorApplicationResponse.builder()
                .userId(entity.getUserId())
                .headline(entity.getHeadline())
                .expertise(entity.getExpertise())
                .basePrice(entity.getBasePrice())
                .ratingAvg(entity.getRatingAvg())
                .sessionsCompleted(entity.getSessionsCompleted())
                .verificationStatus(entity.getVerificationStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Map MentorProfile entity → VerificationStatusResponse.
     */
    public VerificationStatusResponse toVerificationStatusResponse(MentorProfile entity) {
        return VerificationStatusResponse.builder()
                .currentStatus(entity.getVerificationStatus())
                .lastUpdatedAt(entity.getUpdatedAt())
                .build();
    }
}
