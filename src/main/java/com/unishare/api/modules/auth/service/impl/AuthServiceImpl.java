package com.unishare.api.modules.auth.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.exception.AuthErrorCode;
import com.unishare.api.infrastructure.mail.MailService;
import com.unishare.api.infrastructure.security.JwtService;
import com.unishare.api.modules.auth.entity.*;
import com.unishare.api.modules.auth.repository.*;
import com.unishare.api.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int OTP_TTL_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    // ------------------------------------------------------------------ register
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AuthErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email đã được sử dụng: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setEmailVerified(false);
        user.setStatus("pending");
        userRepository.save(user);

        UserCredential credential = new UserCredential(user,
                passwordEncoder.encode(request.getPassword()));
        userCredentialRepository.save(credential);

        Role buyerRole = roleRepository.findByName("BUYER")
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Role BUYER not found"));
        UserRole userRole = new UserRole();
        userRole.getId().setUserId(user.getId());
        userRole.getId().setRoleId(buyerRole.getId());
        userRole.setUser(user);
        userRole.setRole(buyerRole);
        user.getUserRoles().add(userRole);
        userRepository.save(user);

        sendVerificationEmail(user.getId());
        log.info("[Auth] Registered user: {}", user.getEmail());

        return buildAuthResponse(user, List.of("BUYER"));
    }

    // ------------------------------------------------------------------ login
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_CREDENTIALS,
                        "Email hoặc mật khẩu không đúng"));

        UserCredential credential = userCredentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_CREDENTIALS,
                        "Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            log.warn("[Auth] Invalid password attempt for: {}", request.getEmail());
            throw new AppException(AuthErrorCode.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(AuthErrorCode.EMAIL_NOT_VERIFIED,
                    "Vui lòng xác minh email trước khi đăng nhập");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(AuthErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        log.info("[Auth] User logged in: {}", user.getEmail());
        return buildAuthResponse(user, roles);
    }

    // ------------------------------------------------------------------ refreshToken
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_TOKEN, "Refresh token không hợp lệ"));

        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AppException(AuthErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        String newAccessToken = jwtService.generateAccessToken(user.getId(), roles);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(stored.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    // ------------------------------------------------------------------ logout
    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                    log.info("[Auth] User {} logged out", rt.getUserId());
                });
    }

    // ------------------------------------------------------------------ sendVerificationEmail
    @Override
    @Transactional
    public void sendVerificationEmail(Long userId) {
        String otp = generateOtp();
        OtpToken otpToken = OtpToken.of(userId, otp, OtpType.EMAIL_VERIFY, OTP_TTL_MINUTES);
        otpTokenRepository.save(otpToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));
        mailService.sendEmailVerification(user.getEmail(), otp);
        log.info("[Auth] Verification email sent to userId={}", userId);
    }

    // ------------------------------------------------------------------ verifyEmail
    @Override
    @Transactional
    public void verifyEmail(Long userId, String code) {
        OtpToken otp = otpTokenRepository
                .findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(userId, OtpType.EMAIL_VERIFY)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Mã OTP không hợp lệ"));

        if (otp.isExpired()) {
            throw new AppException(AuthErrorCode.OTP_EXPIRED, "Mã OTP đã hết hạn");
        }
        if (!otp.getCode().equals(code)) {
            throw new AppException(AuthErrorCode.INVALID_OTP, "Mã OTP không đúng");
        }

        otp.setUsed(true);
        otpTokenRepository.save(otp);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));
        user.setEmailVerified(true);
        user.setStatus("active");
        userRepository.save(user);
        log.info("[Auth] Email verified for userId={}", userId);
    }

    // ------------------------------------------------------------------ forgotPassword
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Silently ignore unknown emails to prevent user enumeration
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String otp = generateOtp();
            OtpToken otpToken = OtpToken.of(user.getId(), otp, OtpType.PASSWORD_RESET, OTP_TTL_MINUTES);
            otpTokenRepository.save(otpToken);
            mailService.sendPasswordReset(user.getEmail(), otp);
            log.info("[Auth] Password reset OTP sent to userId={}", user.getId());
        });
    }

    // ------------------------------------------------------------------ resetPassword
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        OtpToken otp = otpTokenRepository
                .findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(user.getId(), OtpType.PASSWORD_RESET)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Mã OTP không hợp lệ"));

        if (otp.isExpired()) {
            throw new AppException(AuthErrorCode.OTP_EXPIRED, "Mã OTP đã hết hạn");
        }
        if (!otp.getCode().equals(request.getCode())) {
            throw new AppException(AuthErrorCode.INVALID_OTP, "Mã OTP không đúng");
        }

        otp.setUsed(true);
        otpTokenRepository.save(otp);

        userCredentialRepository.findByUserId(user.getId()).ifPresent(credential -> {
            credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            credential.setUpdatedAt(Instant.now());
            userCredentialRepository.save(credential);
        });

        // Revoke all refresh tokens so other sessions are invalidated
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("[Auth] Password reset for userId={}", user.getId());
    }

    // ------------------------------------------------------------------ helpers
    private AuthResponse buildAuthResponse(User user, List<String> roles) {
        String accessToken = jwtService.generateAccessToken(user.getId(), roles);
        String rawRefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.of(user.getId(), rawRefreshToken,
                jwtService.getRefreshTokenExpiry());
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}

