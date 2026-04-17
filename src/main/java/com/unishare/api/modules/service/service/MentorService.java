package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemRequest;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemResponse;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileRequest;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileResponse;
import com.unishare.api.modules.service.dto.MentorDto.ServicePackageResponse;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MentorService {
    MentorProfileResponse getMentorProfile(UUID mentorId);

    MentorProfileResponse createOrUpdateProfile(UUID userId, MentorProfileRequest request);

    Page<MentorProfileResponse> getAllVerifiedMentors(Pageable pageable);

    Page<ServicePackageResponse> getMentorPackages(UUID mentorId, Pageable pageable);

    ServicePackageResponse createPackage(UUID mentorId, CreateServicePackageRequest request);

    void deletePackage(UUID mentorId, UUID packageId);

    CurriculumItemResponse addCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, CurriculumItemRequest request);

    Page<CurriculumItemResponse> listCurriculum(UUID mentorId, UUID packageId, UUID versionId, Pageable pageable);

    void deleteCurriculumItem(UUID mentorId, UUID curriculumId);
}
