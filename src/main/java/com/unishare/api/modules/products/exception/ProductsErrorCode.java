package com.unishare.api.modules.products.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductsErrorCode implements ExceptionCode {
    CATEGORY_NOT_FOUND(404, "CATEGORY_NOT_FOUND"),
    DOCUMENT_NOT_FOUND(404, "DOCUMENT_NOT_FOUND"),
    ASSET_NOT_FOUND(404, "ASSET_NOT_FOUND"),
    INVALID_DOCUMENT_STATUS(400, "INVALID_DOCUMENT_STATUS");

    private final Integer code;
    private final String type;
}
