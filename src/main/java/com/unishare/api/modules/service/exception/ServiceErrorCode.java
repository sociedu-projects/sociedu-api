package com.unishare.api.modules.service.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceErrorCode implements ExceptionCode {
    SERVICE_VERSION_NOT_FOUND(404, "SERVICE_VERSION_NOT_FOUND"),
    PACKAGE_INACTIVE(400, "PACKAGE_INACTIVE"),
    MENTOR_NOT_FOUND(404, "MENTOR_NOT_FOUND"),
    PACKAGE_NOT_FOUND(404, "PACKAGE_NOT_FOUND"),
    CURRICULUM_NOT_FOUND(404, "CURRICULUM_NOT_FOUND"),
    PACKAGE_CURRICULUM_REQUIRED(400, "PACKAGE_CURRICULUM_REQUIRED"),
    DUPLICATE_CURRICULUM_ORDER_INDEX(400, "DUPLICATE_CURRICULUM_ORDER_INDEX");

    private final Integer code;
    private final String type;
}
