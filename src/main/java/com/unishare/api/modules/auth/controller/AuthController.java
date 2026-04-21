package com.unishare.api.modules.auth.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.dto.response.MeResponse;
import com.unishare.api.modules.auth.dto.response.SessionResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

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

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest http) {
        AuthResponse data = authService.login(request, clientIp(http), http.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build()
                .withData(data)
                .withMessage("Đăng nhập thành công."));
    }

    @Operation(summary = "Làm mới token (rotation)")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest http) {
        AuthResponse data = authService.refreshToken(request, clientIp(http), http.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.<AuthResponse>build().withData(data));
    }

    @Operation(summary = "Đăng xuất")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đăng xuất thành công."));
    }

    @Operation(summary = "Xác minh email")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Xác minh email thành công."));
    }

    @Operation(summary = "Gửi lại email xác minh")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Nếu email tồn tại và chưa xác minh, email chứa liên kết đã được gửi."));
    }

    @Operation(summary = "Quên mật khẩu")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build()
                .withMessage("Nếu email tồn tại, liên kết đặt lại mật khẩu đã được gửi."));
    }

    @Operation(summary = "Đặt lại mật khẩu")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đặt lại mật khẩu thành công."));
    }

    // ---------------------------------------------------------------- new endpoints

    /** GET /api/v1/auth/me — thông tin phiên hiện tại: user + roles + capabilities. */
    @Operation(summary = "Thông tin phiên hiện tại")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        MeResponse data = authService.getMe(userId);
        return ResponseEntity.ok(ApiResponse.<MeResponse>build().withData(data));
    }

    /** POST /api/v1/auth/change-password — đổi mật khẩu, giữ nguyên phiên hiện tại, revoke các phiên khác. */
    @Operation(summary = "Đổi mật khẩu")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "X-Refresh-Token", required = false) String currentRefreshToken) {
        UUID userId = principal.getUserId();
        authService.changePassword(userId, request, currentRefreshToken);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đổi mật khẩu thành công."));
    }

    /** GET /api/v1/auth/sessions — danh sách phiên đăng nhập còn hiệu lực. */
    @Operation(summary = "Danh sách phiên đăng nhập")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> sessions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestHeader(value = "X-Refresh-Token", required = false) String currentRefreshToken) {
        UUID userId = principal.getUserId();
        List<SessionResponse> data = authService.listSessions(userId, currentRefreshToken);
        return ResponseEntity.ok(ApiResponse.<List<SessionResponse>>build().withData(data));
    }

    /** DELETE /api/v1/auth/sessions/{id} — thu hồi một phiên cụ thể. */
    @Operation(summary = "Thu hồi một phiên đăng nhập")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id) {
        UUID userId = principal.getUserId();
        authService.revokeSession(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đã thu hồi phiên đăng nhập."));
    }

    /** POST /api/v1/auth/sessions/revoke-all — đăng xuất khỏi mọi thiết bị. */
    @Operation(summary = "Đăng xuất khỏi mọi thiết bị")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        authService.revokeAllSessions(userId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đã thu hồi tất cả phiên đăng nhập."));
    }

    // ---------------------------------------------------------------- helpers
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }
}
