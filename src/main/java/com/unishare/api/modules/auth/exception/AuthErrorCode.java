package com.unishare.api.modules.auth.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ExceptionCode {

    // Registration
    EMAIL_ALREADY_EXISTS(409, "EMAIL_ALREADY_EXISTS"),

    // Login
    INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS"),
    EMAIL_NOT_VERIFIED(403, "EMAIL_NOT_VERIFIED"),
    ACCOUNT_DISABLED(403, "ACCOUNT_DISABLED"),

    // Token
    INVALID_TOKEN(401, "INVALID_TOKEN"),
    TOKEN_EXPIRED(401, "TOKEN_EXPIRED"),

    // OTP
    INVALID_OTP(400, "INVALID_OTP"),
    OTP_EXPIRED(400, "OTP_EXPIRED"),
    OTP_ALREADY_USED(400, "OTP_ALREADY_USED"),

    // General
    USER_NOT_FOUND(404, "USER_NOT_FOUND"),
    ACCESS_DENIED(403, "ACCESS_DENIED");

    private final Integer code;
    private final String type;
}
