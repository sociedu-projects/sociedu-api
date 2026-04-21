package com.unishare.api.modules.mentor_request.service;

import com.unishare.api.common.dto.PageResponse;
import com.unishare.api.modules.mentor_request.dto.request.ApproveMentorRequestRequest;
import com.unishare.api.modules.mentor_request.dto.request.RejectMentorRequestRequest;
import com.unishare.api.modules.mentor_request.dto.response.MentorRequestResponse;

import java.util.UUID;

/**
 * Use-case dành cho <b>admin/moderator</b> review đơn apply mentor.
 * Tách rời khỏi {@link MentorRequestService} để đảm bảo Single Responsibility +
 * Interface Segregation cho hai nhóm consumer khác nhau (controller user vs controller admin).
 */
public interface AdminMentorRequestService {

    /** List đơn có filter status/q + phân trang. */
    PageResponse<MentorRequestResponse> list(String status, String q, int page, int size);

    /** Chi tiết 1 đơn (kèm thông tin applicant). */
    MentorRequestResponse getDetail(UUID requestId);

    /** Approve → cấp role MENTOR + tạo {@code MentorProfile} + publish event. */
    MentorRequestResponse approve(UUID requestId, UUID adminId, ApproveMentorRequestRequest body);

    /** Reject → set REJECTED + gửi thông báo cho user. */
    MentorRequestResponse reject(UUID requestId, UUID adminId, RejectMentorRequestRequest body);
}
