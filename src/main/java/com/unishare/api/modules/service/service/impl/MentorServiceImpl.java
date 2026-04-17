package com.unishare.api.modules.service.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.constants.MentorVerificationStatuses;
import com.unishare.api.modules.service.exception.ServiceErrorCode;
import com.unishare.api.modules.service.dto.MentorDto.*;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.entity.MentorProfile;
import com.unishare.api.modules.service.entity.PackageCurriculum;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;
import com.unishare.api.modules.service.repository.MentorProfileRepository;
import com.unishare.api.modules.service.repository.PackageCurriculumRepository;
import com.unishare.api.modules.service.repository.ServicePackageRepository;
import com.unishare.api.modules.service.repository.ServicePackageVersionRepository;
import com.unishare.api.modules.service.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageVersionRepository servicePackageVersionRepository;
    private final PackageCurriculumRepository packageCurriculumRepository;

    @Override
    @Transactional(readOnly = true)
    public MentorProfileResponse getMentorProfile(UUID mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.MENTOR_NOT_FOUND, "Mentor not found"));

        List<ServicePackage> packages = servicePackageRepository.findByMentorId(mentorId);

        return MentorProfileResponse.builder()
                .userId(profile.getUserId())
                .headline(profile.getHeadline())
                .expertise(profile.getExpertise())
                .basePrice(profile.getBasePrice())
                .ratingAvg(profile.getRatingAvg())
                .sessionsCompleted(profile.getSessionsCompleted())
                .verificationStatus(profile.getVerificationStatus())
                .packages(packages.stream().map(this::mapToPackageResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public MentorProfileResponse createOrUpdateProfile(UUID userId, MentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElse(new MentorProfile());

        profile.setUserId(userId);
        profile.setHeadline(request.getHeadline());
        profile.setExpertise(request.getExpertise());
        profile.setBasePrice(request.getBasePrice());

        if (profile.getVerificationStatus() == null) {
            profile.setVerificationStatus(MentorVerificationStatuses.PENDING);
        }

        mentorProfileRepository.save(profile);
        return getMentorProfile(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorProfileResponse> getAllVerifiedMentors() {
        return mentorProfileRepository.findByVerificationStatus("verified").stream()
                .map(profile -> getMentorProfile(profile.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getMentorPackages(UUID mentorId) {
        return servicePackageRepository.findByMentorId(mentorId).stream()
                .map(this::mapToPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServicePackageResponse createPackage(UUID mentorId, CreateServicePackageRequest request) {
        validateCreatePackageRequest(request);

        ServicePackage pkg = new ServicePackage();
        pkg.setMentorId(mentorId);
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setIsActive(true);
        ServicePackage saved = servicePackageRepository.save(pkg);

        ServicePackageVersion ver = new ServicePackageVersion();
        ver.setPackageId(saved.getId());
        ver.setPrice(request.getPrice());
        ver.setDuration(request.getDuration());
        ver.setDeliveryType(request.getDeliveryType());
        ver.setIsDefault(true);
        ServicePackageVersion savedVersion = servicePackageVersionRepository.save(ver);

        List<PackageCurriculum> curriculums = request.getCurriculums().stream()
                .map(item -> mapCurriculumRequest(savedVersion.getId(), item))
                .toList();
        packageCurriculumRepository.saveAll(curriculums);

        return mapToPackageResponse(saved);
    }

    @Override
    @Transactional
    public void deletePackage(UUID mentorId, UUID packageId) {
        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .filter(p -> p.getMentorId().equals(mentorId))
                .orElseThrow(() -> new AppException(ServiceErrorCode.PACKAGE_NOT_FOUND, "Package not found"));
        List<ServicePackageVersion> versions = servicePackageVersionRepository.findByPackageId(packageId);
        for (ServicePackageVersion v : versions) {
            packageCurriculumRepository.deleteByPackageVersionId(v.getId());
            servicePackageVersionRepository.delete(v);
        }
        servicePackageRepository.delete(pkg);
    }

    private ServicePackageResponse mapToPackageResponse(ServicePackage pkg) {
        List<ServicePackageVersion> versions = servicePackageVersionRepository.findByPackageId(pkg.getId());
        Map<UUID, List<CurriculumItemResponse>> curriculumsByVersionId = versions.stream()
                .collect(Collectors.toMap(
                        ServicePackageVersion::getId,
                        version -> packageCurriculumRepository.findByPackageVersionIdOrderByOrderIndexAsc(version.getId()).stream()
                                .map(this::mapCurriculum)
                                .toList()
                ));
        return ServicePackageResponse.builder()
                .id(pkg.getId())
                .mentorId(pkg.getMentorId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .isActive(pkg.getIsActive())
                .versions(versions.stream()
                        .map(version -> mapVersion(version, curriculumsByVersionId.getOrDefault(version.getId(), List.of())))
                        .collect(Collectors.toList()))
                .build();
    }

    private ServicePackageVersionResponse mapVersion(ServicePackageVersion v, List<CurriculumItemResponse> curriculums) {
        return ServicePackageVersionResponse.builder()
                .id(v.getId())
                .price(v.getPrice())
                .duration(v.getDuration())
                .deliveryType(v.getDeliveryType())
                .isDefault(v.getIsDefault())
                .curriculums(curriculums)
                .build();
    }

    @Override
    @Transactional
    public CurriculumItemResponse addCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, CurriculumItemRequest request) {
        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .filter(p -> p.getMentorId().equals(mentorId))
                .orElseThrow(() -> new AppException(ServiceErrorCode.PACKAGE_NOT_FOUND, "Package not found"));
        ServicePackageVersion ver = servicePackageVersionRepository.findById(versionId)
                .filter(v -> v.getPackageId().equals(pkg.getId()))
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND, "Version not found"));
        PackageCurriculum c = new PackageCurriculum();
        c.setPackageVersionId(ver.getId());
        c.setTitle(request.getTitle());
        c.setDescription(request.getDescription());
        c.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        c.setDuration(request.getDuration());
        c = packageCurriculumRepository.save(c);
        return mapCurriculum(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurriculumItemResponse> listCurriculum(UUID mentorId, UUID packageId, UUID versionId) {
        assertPackageOwner(mentorId, packageId);
        ServicePackageVersion ver = servicePackageVersionRepository.findById(versionId)
                .filter(v -> v.getPackageId().equals(packageId))
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND, "Version not found"));
        return packageCurriculumRepository.findByPackageVersionIdOrderByOrderIndexAsc(ver.getId()).stream()
                .map(this::mapCurriculum)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCurriculumItem(UUID mentorId, UUID curriculumId) {
        PackageCurriculum c = packageCurriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.CURRICULUM_NOT_FOUND, "Curriculum not found"));
        ServicePackageVersion ver = servicePackageVersionRepository.findById(c.getPackageVersionId())
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND, "Version not found"));
        assertPackageOwner(mentorId, ver.getPackageId());
        packageCurriculumRepository.delete(c);
    }

    private void assertPackageOwner(UUID mentorId, UUID packageId) {
        servicePackageRepository.findById(packageId)
                .filter(p -> p.getMentorId().equals(mentorId))
                .orElseThrow(() -> new AppException(ServiceErrorCode.PACKAGE_NOT_FOUND, "Package not found"));
    }

    private CurriculumItemResponse mapCurriculum(PackageCurriculum c) {
        return CurriculumItemResponse.builder()
                .id(c.getId())
                .packageVersionId(c.getPackageVersionId())
                .title(c.getTitle())
                .description(c.getDescription())
                .orderIndex(c.getOrderIndex())
                .duration(c.getDuration())
                .build();
    }

    private PackageCurriculum mapCurriculumRequest(UUID versionId, CreateServicePackageRequest.CurriculumRequest request) {
        PackageCurriculum curriculum = new PackageCurriculum();
        curriculum.setPackageVersionId(versionId);
        curriculum.setTitle(request.getTitle());
        curriculum.setDescription(request.getDescription());
        curriculum.setOrderIndex(request.getOrderIndex());
        curriculum.setDuration(request.getDuration());
        return curriculum;
    }

    private void validateCreatePackageRequest(CreateServicePackageRequest request) {
        if (request.getCurriculums() == null || request.getCurriculums().isEmpty()) {
            throw new AppException(ServiceErrorCode.PACKAGE_CURRICULUM_REQUIRED,
                    "Gói dịch vụ phải có ít nhất một curriculum");
        }

        Set<Integer> orderIndexes = request.getCurriculums().stream()
                .map(CreateServicePackageRequest.CurriculumRequest::getOrderIndex)
                .collect(Collectors.toSet());
        if (orderIndexes.size() != request.getCurriculums().size()) {
            throw new AppException(ServiceErrorCode.DUPLICATE_CURRICULUM_ORDER_INDEX,
                    "Thứ tự curriculum không được trùng nhau trong cùng một gói");
        }
    }
}
