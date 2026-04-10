package com.unishare.api.modules.profile.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileErrorCode implements ExceptionCode {

    PROFILE_NOT_FOUND(404, "PROFILE_NOT_FOUND"),
    MENTOR_PROFILE_NOT_FOUND(404, "MENTOR_PROFILE_NOT_FOUND"),
    ALREADY_APPLIED_MENTOR(409, "ALREADY_APPLIED_MENTOR"),
    INVALID_FILE_TYPE(400, "INVALID_FILE_TYPE"),
    FILE_TOO_LARGE(400, "FILE_TOO_LARGE"),
    MENTOR_PROFILE_NOT_EDITABLE(400, "MENTOR_PROFILE_NOT_EDITABLE"),
    PAYOUT_INFO_NOT_FOUND(404, "PAYOUT_INFO_NOT_FOUND"),
    PAYOUT_INFO_REQUIRED(400, "PAYOUT_INFO_REQUIRED"),
    VERIFICATION_DOC_NOT_FOUND(404, "VERIFICATION_DOC_NOT_FOUND");

    private final Integer code;
    private final String type;
}
