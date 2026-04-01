package com.unishare.api.modules.order.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ExceptionCode {
    ORDER_NOT_FOUND(404, "ORDER_NOT_FOUND"),
    PAYMENT_FAILED(402, "PAYMENT_FAILED"),
    PAYMENT_INVALID_SIGNATURE(400, "PAYMENT_INVALID_SIGNATURE");

    private final Integer code;
    private final String type;
}
