package com.unishare.api.modules.booking.exception;

import com.unishare.api.common.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingErrorCode implements ExceptionCode {
    BOOKING_NOT_FOUND(404, "BOOKING_NOT_FOUND"),
    BOOKING_ACCESS_DENIED(403, "BOOKING_ACCESS_DENIED"),
    SESSION_NOT_FOUND(404, "SESSION_NOT_FOUND");

    private final Integer code;
    private final String type;
}
