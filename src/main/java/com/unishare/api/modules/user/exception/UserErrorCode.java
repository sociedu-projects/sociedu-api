package com.unishare.api.modules.user.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ExceptionCode {

    PROFILE_NOT_FOUND(404, "PROFILE_NOT_FOUND"),
    EDUCATION_NOT_FOUND(404, "EDUCATION_NOT_FOUND"),
    LANGUAGE_NOT_FOUND(404, "LANGUAGE_NOT_FOUND"),
    EXPERIENCE_NOT_FOUND(404, "EXPERIENCE_NOT_FOUND"),
    CERTIFICATE_NOT_FOUND(404, "CERTIFICATE_NOT_FOUND");

    private final Integer code;
    private final String type;
}
