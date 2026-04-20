package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.MentorDto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MentorService {
    // Mentor Profile
    MentorProfileResponse getMentorProfile(UUID mentorId);
    MentorProfileResponse createOrUpdateProfile(UUID userId, MentorProfileRequest request);

    /**
     * Danh bạ mentor: một GET duy nhất — tìm theo {@code q}, lọc giá / chuyên môn / chỉ đã xác minh, sắp xếp.
     *
     * @param q             từ khóa (tên hiển thị, headline/bio user, headline/expertise mentor), optional
     * @param verifiedOnly  {@code true} = chỉ mentor có {@code mentor_profiles.verification_status = verified}
     * @param maxPrice      giá giờ tối đa ({@code base_price})
     * @param expertise     lặp query: {@code expertise=a&expertise=b} — khớp bất kỳ tag nào (OR)
     * @param sort          {@code popular} | {@code rating} | {@code price-asc} | {@code price-desc}
     */
    List<MentorProfileResponse> searchMentors(
            String q,
            Boolean verifiedOnly,
            BigDecimal maxPrice,
            List<String> expertise,
            String sort);

    // Packages
    List<ServicePackageResponse> getMentorPackages(UUID mentorId);
    ServicePackageResponse createPackage(UUID mentorId, ServicePackageRequest request);
    void deletePackage(UUID mentorId, UUID packageId);

    CurriculumItemResponse addCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, CurriculumItemRequest request);

    List<CurriculumItemResponse> listCurriculum(UUID mentorId, UUID packageId, UUID versionId);

    void deleteCurriculumItem(UUID mentorId, UUID curriculumId);
}
