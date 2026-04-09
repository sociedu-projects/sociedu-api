package com.unishare.api.modules.mentor.service;

import com.unishare.api.modules.mentor.dto.MentorDto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MentorService {

    // ======================== PUBLIC: Mentor Listing ========================

    /**
     * Tìm kiếm và lọc danh sách mentor công khai (đã verified).
     * Hỗ trợ search theo keyword, filter theo expertise/price, sort và pagination.
     */
    Page<MentorListResponse> searchMentors(MentorSearchRequest request, Pageable pageable);

    /**
     * Lấy thông tin hồ sơ chi tiết của một mentor (bao gồm packages và availability preview).
     */
    MentorProfileResponse getMentorProfile(Long mentorId);

    /**
     * Lấy danh sách package active (công khai) của một mentor.
     */
    List<ServicePackageResponse> getPublicMentorPackages(Long mentorId);

    // ======================== MENTOR SELF: Profile ========================

    /**
     * Tạo mới hoặc cập nhật hồ sơ mentor của chính mình.
     */
    MentorProfileResponse createOrUpdateProfile(Long userId, MentorProfileRequest request);

    // ======================== MENTOR SELF: Packages ========================

    /**
     * Lấy tất cả package của mentor đang đăng nhập (bao gồm mọi trạng thái).
     */
    List<ServicePackageResponse> getMyPackages(Long mentorId);

    /**
     * Tạo package dịch vụ mới (trạng thái mặc định: draft).
     */
    ServicePackageResponse createPackage(Long mentorId, ServicePackageRequest request);

    /**
     * Cập nhật thông tin package (PATCH - partial update).
     */
    ServicePackageResponse updatePackage(Long mentorId, Long packageId, ServicePackageUpdateRequest request);

    /**
     * Bật/tắt trạng thái package (active <-> inactive).
     */
    ServicePackageResponse togglePackageStatus(Long mentorId, Long packageId);

    /**
     * Xoá package (chỉ khi ở trạng thái draft).
     */
    void deletePackage(Long mentorId, Long packageId);

    // ======================== MENTOR SELF: Availability ========================

    /**
     * Tạo slot lịch rảnh (hỗ trợ recurrence: sinh sẵn 4 tuần).
     */
    List<AvailabilitySlotResponse> createSlot(Long mentorId, AvailabilitySlotRequest request);

    /**
     * Cập nhật / chặn / huỷ slot.
     */
    AvailabilitySlotResponse updateSlot(Long mentorId, Long slotId, AvailabilitySlotUpdateRequest request);

    // ======================== PUBLIC: Availability ========================

    /**
     * Xem lịch rảnh available của mentor (public - chỉ hiện slot available).
     */
    List<AvailabilitySlotResponse> getMentorAvailability(Long mentorId);

    // ======================== PUBLIC: Categories ========================

    /**
     * Lấy danh sách danh mục chuyên môn (hierarchical tree).
     */
    List<ExpertiseCategoryResponse> getAllCategories();
}
