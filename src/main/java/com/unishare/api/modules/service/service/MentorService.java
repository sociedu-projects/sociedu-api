package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemRequest;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemResponse;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileRequest;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileResponse;
import com.unishare.api.modules.service.dto.MentorDto.ServicePackageResponse;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.dto.request.CreateServicePackageVersionRequest;
import com.unishare.api.modules.service.dto.request.UpdateServicePackageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MentorService {
    /**
     * Lấy danh sách mentor đã xác minh để hiển thị public.
     *
     * @param pageable thông tin phân trang
     * @return danh sách mentor đã xác minh
     */
    MentorProfileResponse getMentorProfile(UUID mentorId);

    /**
     * Tạo mới hoặc cập nhật hồ sơ mentor của người dùng hiện tại.
     *
     * @param userId ID người dùng hiện tại
     * @param request dữ liệu hồ sơ mentor
     * @return hồ sơ mentor sau khi lưu
     */
    MentorProfileResponse createOrUpdateProfile(UUID userId, MentorProfileRequest request);

    /**
     * Lấy danh sách mentor đã xác minh để hiển thị public.
     *
     * @param pageable thông tin phân trang
     * @return danh sách mentor đã xác minh
     */
    Page<MentorProfileResponse> getAllVerifiedMentors(Pageable pageable);

    /**
     * Lấy danh sách gói dịch vụ đang mở của một mentor.
     *
     * @param mentorId ID mentor
     * @param pageable thông tin phân trang
     * @return danh sách gói đang mở
     */
    Page<ServicePackageResponse> getMentorPackages(UUID mentorId, Pageable pageable);

    /**
     * Lấy danh sách tất cả gói dịch vụ của mentor hiện tại, gồm cả đang bật và đang tắt.
     *
     * @param mentorId ID mentor hiện tại
     * @param pageable thông tin phân trang
     * @return danh sách gói dịch vụ của mentor
     */
    Page<ServicePackageResponse> getMyPackages(UUID mentorId, Pageable pageable);

    /**
     * Lấy danh sách public của các gói dịch vụ đang mở.
     *
     * @param pageable thông tin phân trang
     * @return danh sách gói dịch vụ public
     */
    Page<ServicePackageResponse> getActivePackages(Pageable pageable);

    /**
     * Lấy chi tiết public của một gói dịch vụ đang mở.
     *
     * @param packageId ID gói dịch vụ
     * @return chi tiết gói dịch vụ
     */
    ServicePackageResponse getActivePackage(UUID packageId);

    /**
     * Tạo gói dịch vụ mới cho mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param request dữ liệu gói dịch vụ
     * @return gói dịch vụ vừa tạo
     */
    ServicePackageResponse createPackage(UUID mentorId, CreateServicePackageRequest request);

    /**
     * Tạo version mới cho gói dịch vụ và chuyển version mới thành mặc định.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @param request dữ liệu version mới
     * @return gói dịch vụ sau khi thêm version
     */
    ServicePackageResponse createPackageVersion(UUID mentorId, UUID packageId, CreateServicePackageVersionRequest request);

    /**
     * Cập nhật metadata của gói dịch vụ thuộc mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @param request dữ liệu cập nhật
     * @return gói dịch vụ sau khi cập nhật
     */
    ServicePackageResponse updatePackage(UUID mentorId, UUID packageId, UpdateServicePackageRequest request);

    /**
     * Bật hoặc tắt gói dịch vụ của mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @return gói dịch vụ sau khi đổi trạng thái
     */
    ServicePackageResponse togglePackage(UUID mentorId, UUID packageId);

    /**
     * Xóa gói dịch vụ của mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     */
    void deletePackage(UUID mentorId, UUID packageId);

    /**
     * Thêm một curriculum item vào phiên bản gói thuộc mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @param versionId ID phiên bản gói
     * @param request dữ liệu curriculum
     * @return curriculum item đã lưu
     */
    CurriculumItemResponse addCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, CurriculumItemRequest request);

    /**
     * Cập nhật một curriculum item trong phiên bản gói thuộc mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @param versionId ID phiên bản gói
     * @param curriculumId ID curriculum
     * @param request dữ liệu curriculum mới
     * @return curriculum item sau khi cập nhật
     */
    CurriculumItemResponse updateCurriculumItem(UUID mentorId, UUID packageId, UUID versionId, UUID curriculumId, CurriculumItemRequest request);

    /**
     * Lấy danh sách curriculum của một phiên bản gói thuộc mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param packageId ID gói dịch vụ
     * @param versionId ID phiên bản gói
     * @param pageable thông tin phân trang
     * @return danh sách curriculum
     */
    Page<CurriculumItemResponse> listCurriculum(UUID mentorId, UUID packageId, UUID versionId, Pageable pageable);

    /**
     * Xóa một curriculum item thuộc mentor hiện tại.
     *
     * @param mentorId ID mentor
     * @param curriculumId ID curriculum
     */
    void deleteCurriculumItem(UUID mentorId, UUID curriculumId);
}
