package com.unishare.api.modules.file.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileErrorCode implements ExceptionCode {
    FILE_NOT_FOUND(404, "FILE_NOT_FOUND"),
    FILE_ACCESS_DENIED(403, "FILE_ACCESS_DENIED"),
    FILE_UPLOAD_FAILED(500, "FILE_UPLOAD_FAILED");

    private final Integer code;
    private final String type;
}
