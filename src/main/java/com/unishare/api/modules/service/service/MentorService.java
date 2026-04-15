package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.MentorDto.*;

import java.util.List;

public interface MentorService {
    // Mentor Profile
    MentorProfileResponse getMentorProfile(Long mentorId);
    MentorProfileResponse createOrUpdateProfile(Long userId, MentorProfileRequest request);
    List<MentorProfileResponse> getAllVerifiedMentors();

    // Packages
    List<ServicePackageResponse> getMentorPackages(Long mentorId);
    ServicePackageResponse createPackage(Long mentorId, ServicePackageRequest request);
    void deletePackage(Long mentorId, Long packageId);

    CurriculumItemResponse addCurriculumItem(Long mentorId, Long packageId, Long versionId, CurriculumItemRequest request);

    List<CurriculumItemResponse> listCurriculum(Long mentorId, Long packageId, Long versionId);

    void deleteCurriculumItem(Long mentorId, Long curriculumId);
}
