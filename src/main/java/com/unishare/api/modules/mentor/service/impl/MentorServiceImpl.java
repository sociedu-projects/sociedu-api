package com.unishare.api.modules.mentor.service.impl;

import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.mentor.entity.ServicePackage;
import com.unishare.api.modules.mentor.repository.MentorProfileRepository;
import com.unishare.api.modules.mentor.repository.ServicePackageRepository;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ServicePackageRepository servicePackageRepository;

    @Override
    @Transactional(readOnly = true)
    public MentorProfileResponse getMentorProfile(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        
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
    public MentorProfileResponse createOrUpdateProfile(Long userId, MentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElse(new MentorProfile());
        
        profile.setUserId(userId);
        profile.setHeadline(request.getHeadline());
        profile.setExpertise(request.getExpertise());
        profile.setBasePrice(request.getBasePrice());
        
        if (profile.getVerificationStatus() == null) {
            profile.setVerificationStatus("pending");
        }
        
        MentorProfile saved = mentorProfileRepository.save(profile);
        return getMentorProfile(saved.getUserId());
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
    public List<ServicePackageResponse> getMentorPackages(Long mentorId) {
        return servicePackageRepository.findByMentorId(mentorId).stream()
                .map(this::mapToPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServicePackageResponse createPackage(Long mentorId, ServicePackageRequest request) {
        ServicePackage pkg = new ServicePackage();
        pkg.setMentorId(mentorId);
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setDuration(request.getDuration());
        pkg.setPrice(request.getPrice());
        pkg.setDeliveryType(request.getDeliveryType());
        
        ServicePackage saved = servicePackageRepository.save(pkg);
        return mapToPackageResponse(saved);
    }

    @Override
    @Transactional
    public void deletePackage(Long mentorId, Long packageId) {
        ServicePackage pkg = servicePackageRepository.findById(packageId)
                .filter(p -> p.getMentorId().equals(mentorId))
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));
        servicePackageRepository.delete(pkg);
    }

    private ServicePackageResponse mapToPackageResponse(ServicePackage pkg) {
        return ServicePackageResponse.builder()
                .id(pkg.getId())
                .mentorId(pkg.getMentorId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .duration(pkg.getDuration())
                .price(pkg.getPrice())
                .deliveryType(pkg.getDeliveryType())
                .build();
    }
}
