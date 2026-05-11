package com.unishare.api.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendPhoneOtpRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Số điện thoại không hợp lệ (định dạng E.164)")
    private String phoneNumber;
}
