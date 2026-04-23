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
}
