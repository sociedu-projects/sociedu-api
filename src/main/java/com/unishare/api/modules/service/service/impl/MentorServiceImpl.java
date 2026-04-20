package com.unishare.api.modules.service.service.impl;

import com.unishare.api.common.constants.Roles;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.constants.MentorVerificationStatuses;
import com.unishare.api.modules.auth.entity.User;
import com.unishare.api.modules.auth.repository.UserRepository;
import com.unishare.api.modules.service.exception.ServiceErrorCode;
import com.unishare.api.modules.service.dto.MentorDto.*;
import com.unishare.api.modules.service.entity.MentorProfile;
import com.unishare.api.modules.service.entity.PackageCurriculum;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;
import com.unishare.api.modules.service.repository.MentorProfileRepository;
import com.unishare.api.modules.service.repository.PackageCurriculumRepository;
import com.unishare.api.modules.service.repository.ServicePackageRepository;
import com.unishare.api.modules.service.repository.ServicePackageVersionRepository;
import com.unishare.api.modules.service.service.MentorService;
import com.unishare.api.modules.user.entity.UserProfile;
import com.unishare.api.modules.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ServicePackageVersionRepository servicePackageVersionRepository;
    private final PackageCurriculumRepository packageCurriculumRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public MentorProfileResponse getMentorProfile(UUID mentorId) {
        return mentorProfileRepository.findById(mentorId)
                .map(mp -> toFullMentorResponse(mp))
                .orElseGet(() -> buildResponseForMentorWithoutServiceProfile(mentorId));
    }

    /** Có đủ {@link MentorProfile} + gói — dùng cho chi tiết. */
    private MentorProfileResponse toFullMentorResponse(MentorProfile profile) {
        UUID mentorId = profile.getUserId();
        List<ServicePackage> packages = servicePackageRepository.findByMentorId(mentorId);
        UserProfile up = userProfileRepository.findById(mentorId).orElse(null);

        return MentorProfileResponse.builder()
                .userId(profile.getUserId())
                .displayName(up != null ? up.getDisplayName() : null)
                .headline(profile.getHeadline())
                .expertise(profile.getExpertise())
                .basePrice(profile.getBasePrice())
                .ratingAvg(profile.getRatingAvg())
                .sessionsCompleted(profile.getSessionsCompleted())
                .verificationStatus(profile.getVerificationStatus())
                .packages(packages.stream().map(this::mapToPackageResponse).collect(Collectors.toList()))
                .build();
    }

    /**
     * User có role MENTOR nhưng chưa tạo {@code mentor_profiles} — vẫn trả JSON để client hiển thị stub.
     */
    private MentorProfileResponse buildResponseForMentorWithoutServiceProfile(UUID mentorId) {
        User user = userRepository.findByIdWithRoles(mentorId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.MENTOR_NOT_FOUND, "Mentor not found"));
        if (!userHasMentorRole(user)) {
            throw new AppException(ServiceErrorCode.MENTOR_NOT_FOUND, "Mentor not found");
        }
        UserProfile up = userProfileRepository.findById(mentorId).orElse(null);
        return toDirectoryItem(user, null, up);
    }

    private static boolean userHasMentorRole(User user) {
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null && Roles.MENTOR.equals(ur.getRole().getName()));
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
    public List<MentorProfileResponse> searchMentors(
            String q,
            Boolean verifiedOnly,
            BigDecimal maxPrice,
            List<String> expertise,
            String sort) {

        List<User> withRole = userRepository.findAllWithRoleName(Roles.MENTOR);
        if (withRole.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = withRole.stream().map(User::getId).toList();
        Map<UUID, MentorProfile> mpMap = mentorProfileRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(MentorProfile::getUserId, Function.identity()));
        Map<UUID, UserProfile> upMap = userProfileRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        boolean onlyVerified = Boolean.TRUE.equals(verifiedOnly);

        List<MentorProfileResponse> out = new ArrayList<>();
        for (User u : withRole) {
            MentorProfile mp = mpMap.get(u.getId());
            UserProfile up = upMap.get(u.getId());
            if (!matchesVerified(mp, onlyVerified)) {
                continue;
            }
            if (!matchesQ(mp, up, q)) {
                continue;
            }
            if (!matchesMaxPrice(mp, maxPrice)) {
                continue;
            }
            if (!matchesExpertiseAny(mp, expertise)) {
                continue;
            }
            out.add(toDirectoryItem(u, mp, up));
        }

        out.sort(responseComparator(sort));
        return out;
    }

    private static boolean matchesVerified(MentorProfile mp, boolean onlyVerified) {
        if (!onlyVerified) {
            return true;
        }
        return mp != null && MentorVerificationStatuses.VERIFIED.equalsIgnoreCase(mp.getVerificationStatus());
    }

    private static boolean matchesQ(MentorProfile mp, UserProfile up, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String needle = q.trim().toLowerCase();
        if (up != null) {
            if (up.getDisplayName().toLowerCase().contains(needle)) {
                return true;
            }
            if (up.getHeadline() != null && up.getHeadline().toLowerCase().contains(needle)) {
                return true;
            }
            if (up.getBio() != null && up.getBio().toLowerCase().contains(needle)) {
                return true;
            }
        }
        if (mp != null) {
            if (mp.getHeadline() != null && mp.getHeadline().toLowerCase().contains(needle)) {
                return true;
            }
            if (mp.getExpertise() != null && mp.getExpertise().toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMaxPrice(MentorProfile mp, BigDecimal maxPrice) {
        if (maxPrice == null) {
            return true;
        }
        if (mp == null || mp.getBasePrice() == null) {
            return true;
        }
        return mp.getBasePrice().compareTo(maxPrice) <= 0;
    }

    private static boolean matchesExpertiseAny(MentorProfile mp, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }
        String bucket = (mp != null && mp.getExpertise() != null) ? mp.getExpertise().toLowerCase() : "";
        for (String tag : tags) {
            if (tag != null && !tag.isBlank() && bucket.contains(tag.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static Comparator<MentorProfileResponse> responseComparator(String sort) {
        String s = sort == null || sort.isBlank() ? "popular" : sort.trim().toLowerCase();
        return switch (s) {
            case "rating" -> Comparator
                    .comparing((MentorProfileResponse r) -> r.getRatingAvg() != null ? r.getRatingAvg() : 0f)
                    .reversed();
            case "price-asc" -> Comparator.comparing(
                    MentorProfileResponse::getBasePrice,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "price-desc" -> Comparator.comparing(
                    MentorProfileResponse::getBasePrice,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator
                    .comparing((MentorProfileResponse r) -> r.getSessionsCompleted() != null ? r.getSessionsCompleted() : 0)
                    .reversed();
        };
    }

    /** Danh sách: không nạp gói; ghép tên từ {@code user_profiles} + mentor service profile. */
    private MentorProfileResponse toDirectoryItem(User u, MentorProfile mp, UserProfile up) {
        String displayName = up != null ? up.getDisplayName() : "Người dùng";
        String headline;
        if (mp != null && notBlank(mp.getHeadline())) {
            headline = mp.getHeadline();
        } else if (up != null && notBlank(up.getHeadline())) {
            headline = up.getHeadline();
        } else {
            headline = "Chưa cập nhật hồ sơ mentor";
        }

        return MentorProfileResponse.builder()
                .userId(u.getId())
                .displayName(displayName)
                .headline(headline)
                .expertise(mp != null ? mp.getExpertise() : null)
                .basePrice(mp != null ? mp.getBasePrice() : null)
                .ratingAvg(mp != null ? mp.getRatingAvg() : 0f)
                .sessionsCompleted(mp != null ? mp.getSessionsCompleted() : 0)
                .verificationStatus(mp != null ? mp.getVerificationStatus() : MentorVerificationStatuses.PENDING)
                .packages(List.of())
                .build();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
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
    public ServicePackageResponse createPackage(UUID mentorId, ServicePackageRequest request) {
        ServicePackage pkg = new ServicePackage();
        pkg.setMentorId(mentorId);
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setIsActive(true);
        ServicePackage saved = servicePackageRepository.save(pkg);

        ServicePackageVersion ver = new ServicePackageVersion();
        ver.setPackageId(saved.getId());
        ver.setPrice(request.getPrice());
        ver.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        ver.setDeliveryType(request.getDeliveryType());
        ver.setIsDefault(true);
        servicePackageVersionRepository.save(ver);

        return mapToPackageResponse(servicePackageRepository.findById(saved.getId()).orElseThrow());
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
        return ServicePackageResponse.builder()
                .id(pkg.getId())
                .mentorId(pkg.getMentorId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .isActive(pkg.getIsActive())
                .versions(versions.stream().map(this::mapVersion).collect(Collectors.toList()))
                .build();
    }

    private ServicePackageVersionResponse mapVersion(ServicePackageVersion v) {
        return ServicePackageVersionResponse.builder()
                .id(v.getId())
                .price(v.getPrice())
                .duration(v.getDuration())
                .deliveryType(v.getDeliveryType())
                .isDefault(v.getIsDefault())
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
}
