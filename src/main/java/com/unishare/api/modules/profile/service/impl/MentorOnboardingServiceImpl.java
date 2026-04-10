package com.unishare.api.modules.profile.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplyRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplicationResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.UpdateMentorProfileRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationStatusResponse;
import com.unishare.api.modules.profile.exception.ProfileErrorCode;
import com.unishare.api.modules.profile.mapper.ProfileMapper;
import com.unishare.api.modules.profile.repository.ProfileMentorProfileRepository;
import com.unishare.api.modules.profile.service.MentorOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorOnboardingServiceImpl implements MentorOnboardingService {

    private final ProfileMentorProfileRepository mentorProfileRepository;
    private final ProfileMapper profileMapper;

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_REJECTED = "rejected";

    /**
     * Các trạng thái cho phép mentor chỉnh sửa hồ sơ.
     */
    private static final Set<String> EDITABLE_STATUSES = Set.of(STATUS_DRAFT, STATUS_REJECTED);

    @Override
    @Transactional
    public MentorApplicationResponse applyMentor(Long userId, MentorApplyRequest request) {
        checkNotAlreadyApplied(userId);

        MentorProfile profile = new MentorProfile();
        profile.setUserId(userId);
        profile.setHeadline(request.getHeadline());
        profile.setExpertise(request.getExpertise());
        profile.setVerificationStatus(STATUS_DRAFT);

        MentorProfile saved = mentorProfileRepository.save(profile);
        log.info("User {} applied as mentor, status = draft", userId);
        return profileMapper.toMentorApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorApplicationResponse getMyApplication(Long userId) {
        MentorProfile profile = findMentorProfileOrThrow(userId);
        return profileMapper.toMentorApplicationResponse(profile);
    }

    @Override
    @Transactional
    public MentorApplicationResponse updateMentorProfile(Long userId, UpdateMentorProfileRequest request) {
        MentorProfile profile = findMentorProfileOrThrow(userId);
        validateEditable(profile);

        applyPartialUpdate(profile, request);
        MentorProfile saved = mentorProfileRepository.save(profile);

        log.info("User {} updated mentor profile", userId);
        return profileMapper.toMentorApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationStatusResponse getVerificationStatus(Long userId) {
        MentorProfile profile = findMentorProfileOrThrow(userId);
        return profileMapper.toVerificationStatusResponse(profile);
    }

    // ======================== PRIVATE HELPERS ========================

    private MentorProfile findMentorProfileOrThrow(Long userId) {
        return mentorProfileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ProfileErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }

    private void checkNotAlreadyApplied(Long userId) {
        if (mentorProfileRepository.existsById(userId)) {
            throw new AppException(ProfileErrorCode.ALREADY_APPLIED_MENTOR,
                    "User has already applied as mentor");
        }
    }

    /**
     * Chỉ cho phép chỉnh sửa hồ sơ khi status là draft hoặc rejected.
     */
    private void validateEditable(MentorProfile profile) {
        if (!EDITABLE_STATUSES.contains(profile.getVerificationStatus())) {
            throw new AppException(ProfileErrorCode.MENTOR_PROFILE_NOT_EDITABLE,
                    "Mentor profile can only be edited when status is draft or rejected");
        }
    }

    /**
     * Partial update: chỉ set field khi giá trị request không null.
     */
    private void applyPartialUpdate(MentorProfile profile, UpdateMentorProfileRequest request) {
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline());
        }
        if (request.getExpertise() != null) {
            profile.setExpertise(request.getExpertise());
        }
        if (request.getBasePrice() != null) {
            profile.setBasePrice(request.getBasePrice());
        }
    }
}
