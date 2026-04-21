package com.unishare.api.modules.auth.service;

import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.dto.response.MeResponse;
import com.unishare.api.modules.auth.dto.response.SessionResponse;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);

    void logout(RefreshTokenRequest request);

    void sendVerificationEmail(String email);

    void verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    /** Current authenticated user: profile summary + roles + capabilities. */
    MeResponse getMe(UUID userId);

    /** Change password (requires old password), revokes all other sessions. */
    void changePassword(UUID userId, ChangePasswordRequest request, String currentRefreshToken);

    /** List active sessions (refresh tokens) of a user. */
    List<SessionResponse> listSessions(UUID userId, String currentRefreshToken);

    /** Revoke a single session by id (must belong to the user). */
    void revokeSession(UUID userId, UUID sessionId);

    /** Revoke all sessions of the user. */
    void revokeAllSessions(UUID userId);
}
