package com.unishare.api.modules.auth.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    /** POST /api/v1/auth/register — Đăng ký tài khoản mới. */
    @Operation(summary = "Đăng ký")
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
    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build()
                .withData(data)
                .withMessage("Đăng nhập thành công."));
    }

    /** POST /api/v1/auth/refresh — Làm mới Access Token. */
    @Operation(summary = "Làm mới token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse data = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build().withData(data));
    }

    /** POST /api/v1/auth/logout — Đăng xuất, revoke Refresh Token. */
    @Operation(summary = "Đăng xuất")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đăng xuất thành công."));
    }

    /** POST /api/v1/auth/verify-email — Xác minh email bằng token từ liên kết trong mail (không cần đăng nhập). */
    @Operation(summary = "Xác minh email")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse data = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build()
                .withData(data)
                .withMessage("Xác minh email thành công."));
    }

    /** POST /api/v1/auth/resend-verification — Gửi lại email chứa liên kết xác minh (không cần đăng nhập). */
    @Operation(summary = "Gửi lại email xác minh")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Nếu email tồn tại và chưa xác minh, email chứa liên kết đã được gửi."));
    }

    /** POST /api/v1/auth/forgot-password — Gửi email chứa liên kết đặt lại mật khẩu. */
    @Operation(summary = "Quên mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build()
                .withMessage("Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi."));
    }

    /** POST /api/v1/auth/reset-password — Đặt lại mật khẩu bằng token từ liên kết trong mail. */
    @Operation(summary = "Đặt lại mật khẩu")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đặt lại mật khẩu thành công."));
    }
}
