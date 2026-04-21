package com.unishare.api.modules.mentor_request.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Mã lỗi nghiệp vụ cho luồng apply mentor (Phase 2).
 */
@Getter
@AllArgsConstructor
public enum MentorRequestErrorCode implements ExceptionCode {

    REQUEST_NOT_FOUND(404, "MENTOR_REQUEST_NOT_FOUND"),
    ALREADY_MENTOR(409, "ALREADY_MENTOR"),
    HAS_OPEN_REQUEST(409, "HAS_OPEN_REQUEST"),
    CANNOT_RESUBMIT(409, "CANNOT_RESUBMIT"),
    INVALID_STATE(409, "MENTOR_REQUEST_INVALID_STATE"),
    ACCESS_DENIED(403, "MENTOR_REQUEST_ACCESS_DENIED");

    private final Integer code;
    private final String type;
}
