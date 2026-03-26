package com.unishare.api.modules.auth.service;

import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void sendVerificationEmail(Long userId);

    void verifyEmail(Long userId, String code);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

