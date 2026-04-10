package com.unishare.api.modules.profile.mapper;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplicationResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationStatusResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationDocumentResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.PayoutInfoResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorReviewHistoryResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.MyProfileResponse;
import com.unishare.api.modules.profile.entity.VerificationDocument;
import com.unishare.api.modules.profile.entity.MentorPayoutInfo;
import com.unishare.api.modules.profile.entity.MentorReviewHistory;
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
    public VerificationStatusResponse toVerificationStatusResponse(MentorProfile entity, List<MentorReviewHistoryResponse> reviewHistory) {
        return VerificationStatusResponse.builder()
                .currentStatus(entity.getVerificationStatus())
                .lastUpdatedAt(entity.getUpdatedAt())
                .reviewHistory(reviewHistory)
                .build();
    }

    public VerificationDocumentResponse toVerificationDocumentResponse(VerificationDocument entity) {
        return VerificationDocumentResponse.builder()
                .id(entity.getId())
                .fileUrl(entity.getFileUrl())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PayoutInfoResponse toPayoutInfoResponse(MentorPayoutInfo entity) {
        String maskedAcc = maskAccountNumber(entity.getAccountNumber());
        return PayoutInfoResponse.builder()
                .bankName(entity.getBankName())
                .maskedAccountNumber(maskedAcc)
                .accountHolder(entity.getAccountHolder())
                .branch(entity.getBranch())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MentorReviewHistoryResponse toMentorReviewHistoryResponse(MentorReviewHistory entity) {
        return MentorReviewHistoryResponse.builder()
                .fromStatus(entity.getFromStatus())
                .toStatus(entity.getToStatus())
                .reasonCode(entity.getReasonCode())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) return accountNumber;
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
}
