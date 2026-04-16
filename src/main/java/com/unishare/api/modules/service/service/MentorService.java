package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.MentorDto.*;

import java.util.List;
import java.util.UUID;

public interface MentorService {
    // Mentor Profile
    MentorProfileResponse getMentorProfile(UUID mentorId);
    MentorProfileResponse createOrUpdateProfile(UUID userId, MentorProfileRequest request);
    List<MentorProfileResponse> getAllVerifiedMentors();

    // Packages
    List<ServicePackageResponse> getMentorPackages(UUID mentorId);
    ServicePackageResponse createPackage(UUID mentorId, ServicePackageRequest request);
    void deletePackage(UUID mentorId, UUID packageId);

    CurriculumItemResponse addCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, CurriculumItemRequest request);

    List<CurriculumItemResponse> listCurriculum(UUID mentorId, UUID packageId, UUID versionId);

    void deleteCurriculumItem(UUID mentorId, UUID curriculumId);
}
