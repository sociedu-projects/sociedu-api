package com.unishare.api.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyPhoneOtpRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Số điện thoại không hợp lệ (định dạng E.164)")
    private String phoneNumber;

    @NotBlank(message = "Mã OTP không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải có đúng 6 chữ số")
    private String otpCode;
}
