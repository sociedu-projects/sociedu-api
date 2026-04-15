package com.unishare.api.modules.trust.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrustErrorCode implements ExceptionCode {
    REPORT_NOT_FOUND(404, "REPORT_NOT_FOUND"),
    DISPUTE_NOT_FOUND(404, "DISPUTE_NOT_FOUND"),
    TRUST_ACCESS_DENIED(403, "TRUST_ACCESS_DENIED");

    private final Integer code;
    private final String type;
}
