package com.unishare.api.modules.auth.service;

import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.dto.response.MeResponse;
import com.unishare.api.modules.auth.dto.response.SessionResponse;

import java.util.List;
import java.util.UUID;

/**
 * API chính cho module Auth. Các method đọc session (list/revoke) và đổi mật khẩu cần
 * truyền {@code currentRefreshToken} để đánh dấu "phiên hiện tại" và tránh bị tự khoá
 * khi change-password.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);

    void logout(RefreshTokenRequest request);

    void sendVerificationEmail(String email);

    AuthResponse verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    MeResponse getMe(UUID userId);

    void changePassword(UUID userId, ChangePasswordRequest request, String currentRefreshToken);

    List<SessionResponse> listSessions(UUID userId, String currentRefreshToken);

    void revokeSession(UUID userId, UUID sessionId);

    void revokeAllSessions(UUID userId);

    // ---- Flow A: Xác thực số điện thoại (Authenticated) ----

    /** Gửi OTP tới email user để xác thực số điện thoại. */
    void sendPhoneVerificationOtp(UUID userId, SendPhoneOtpRequest request);

    /** Xác thực OTP → gắn phoneNumber vào tài khoản. */
    void verifyPhoneOtp(UUID userId, VerifyPhoneOtpRequest request);

    // ---- Flow C: Đăng nhập bằng email OTP (Public) ----

    /** Gửi OTP đăng nhập tới email. */
    void sendLoginOtp(SendLoginOtpRequest request);

    /** Đăng nhập bằng email + OTP. */
    AuthResponse loginWithOtp(LoginOtpRequest request, String ipAddress, String userAgent);
}
