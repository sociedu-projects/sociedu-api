package com.unishare.api.modules.profile.service;

import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplyRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplicationResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.UpdateMentorProfileRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationStatusResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationDocumentResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.PayoutInfoRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.PayoutInfoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MentorOnboardingService {

    /**
     * Đăng ký làm mentor — tạo MentorProfile draft mới.
     * Throw ALREADY_APPLIED nếu đã tồn tại.
     */
    MentorApplicationResponse applyMentor(Long userId, MentorApplyRequest request);

    /**
     * Xem trạng thái đơn đăng ký mentor của mình.
     */
    MentorApplicationResponse getMyApplication(Long userId);

    /**
     * Cập nhật hồ sơ mentor (headline, expertise, basePrice). Partial update.
     * Chỉ cho phép khi status = draft hoặc rejected.
     */
    MentorApplicationResponse updateMentorProfile(Long userId, UpdateMentorProfileRequest request);

    /**
     * Xem trạng thái xác minh hiện tại của mentor.
     */
    VerificationStatusResponse getVerificationStatus(Long userId);

    /**
     * Upload tài liệu minh chứng xác nhận.
     */
    VerificationDocumentResponse uploadVerificationDocument(Long userId, MultipartFile file);

    /**
     * Thêm hoặc cập nhật thông tin thanh toán (Payout Info).
     */
    PayoutInfoResponse updatePayoutInfo(Long userId, PayoutInfoRequest request);
}
