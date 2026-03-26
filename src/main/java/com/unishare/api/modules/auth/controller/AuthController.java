package com.unishare.api.modules.auth.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/v1/auth/register — Đăng ký tài khoản mới. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>build()
                        .withHttpStatus(HttpStatus.CREATED)
                        .withData(data)
                        .withMessage("Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản."));
    }

    /** POST /api/v1/auth/login — Đăng nhập. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build()
                .withData(data)
                .withMessage("Đăng nhập thành công."));
    }

    /** POST /api/v1/auth/refresh — Làm mới Access Token. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse data = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build().withData(data));
    }

    /** POST /api/v1/auth/logout — Đăng xuất, revoke Refresh Token. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đăng xuất thành công."));
    }

    /** POST /api/v1/auth/verify-email — Xác minh email bằng OTP. */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(principal.getUserId(), request.getCode());
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Xác minh email thành công."));
    }

    /** POST /api/v1/auth/resend-verification — Gửi lại OTP xác minh. */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.sendVerificationEmail(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("OTP xác minh đã được gửi lại."));
    }

    /** POST /api/v1/auth/forgot-password — Gửi OTP reset mật khẩu. */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build()
                .withMessage("Nếu email tồn tại, mã OTP đã được gửi."));
    }

    /** POST /api/v1/auth/reset-password — Đặt lại mật khẩu bằng OTP. */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đặt lại mật khẩu thành công."));
    }
}
