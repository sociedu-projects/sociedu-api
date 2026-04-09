package com.unishare.api.modules.mentor.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MentorErrorCode implements ExceptionCode {

    MENTOR_NOT_FOUND(404, "MENTOR_NOT_FOUND"),
    PACKAGE_NOT_FOUND(404, "PACKAGE_NOT_FOUND"),
    SLOT_NOT_FOUND(404, "SLOT_NOT_FOUND"),
    SLOT_CONFLICT(409, "SLOT_CONFLICT"),
    PACKAGE_NOT_EDITABLE(400, "PACKAGE_NOT_EDITABLE"),
    SLOT_ALREADY_BOOKED(400, "SLOT_ALREADY_BOOKED"),
    MENTOR_NOT_VERIFIED(400, "MENTOR_NOT_VERIFIED"),
    INVALID_TIME_RANGE(400, "INVALID_TIME_RANGE"),
    UNAUTHORIZED_ACCESS(403, "UNAUTHORIZED_ACCESS");

    private final Integer code;
    private final String type;
}
