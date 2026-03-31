package com.unishare.api.modules.mentor.service;

import com.unishare.api.modules.mentor.dto.MentorDto.*;

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
}
