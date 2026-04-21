package com.unishare.api.modules.mentor_request.service;

import com.unishare.api.modules.mentor_request.dto.request.CreateMentorRequestRequest;
import com.unishare.api.modules.mentor_request.dto.response.MentorRequestResponse;

import java.util.UUID;

/**
 * Use-case dành cho <b>user đầu cuối</b> quản lý đơn apply mentor của mình.
 * <p>
 * Tách khỏi {@link AdminMentorRequestService} (SRP + ISP) — controller user chỉ phụ thuộc
 * interface này, không thấy được các API review/approve của admin.
 * </p>
 */
public interface MentorRequestService {

    /**
     * Nộp đơn apply mentor mới. Nếu user đã có đơn APPROVED → ném {@code ALREADY_MENTOR}.
     * Nếu có đơn đang {@code SUBMITTED/UNDER_REVIEW} → ném {@code HAS_OPEN_REQUEST}.
     */
    MentorRequestResponse submit(UUID userId, CreateMentorRequestRequest request);

    /**
     * Resubmit sau khi đơn bị reject. Chỉ cho phép khi đơn gần nhất ở trạng thái
     * {@code REJECTED}. Giữ nguyên rowId, cập nhật payload và trở về {@code SUBMITTED}.
     */
    MentorRequestResponse resubmit(UUID userId, CreateMentorRequestRequest request);

    /** Xem đơn gần nhất của user (có thể null nếu chưa bao giờ nộp). */
    MentorRequestResponse getMyCurrent(UUID userId);
}
