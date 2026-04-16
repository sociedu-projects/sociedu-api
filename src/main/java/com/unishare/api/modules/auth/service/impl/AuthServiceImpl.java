package com.unishare.api.modules.auth.service.impl;

import com.unishare.api.common.constants.Roles;
import com.unishare.api.common.constants.UserStatuses;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.event.DomainEventPublisher;
import com.unishare.api.common.event.EmailVerificationMailEvent;
import com.unishare.api.common.event.PasswordResetMailEvent;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.exception.AuthErrorCode;
import com.unishare.api.infrastructure.security.JwtService;
import com.unishare.api.modules.auth.entity.*;
import com.unishare.api.modules.auth.repository.*;
import com.unishare.api.modules.user.entity.UserProfile;
import com.unishare.api.modules.user.repository.UserProfileRepository;
import com.unishare.api.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int OTP_TTL_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    @Value("${app.auth.email-verification-page-url}")
    private String emailVerificationPageUrl;

    @Value("${app.auth.password-reset-page-url}")
    private String passwordResetPageUrl;

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher eventPublisher;
    private final UserProfileRepository userProfileRepository;

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
        user.setStatus(UserStatuses.PENDING);

        UserCredential credential = new UserCredential();
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCredential(credential);

        Role buyerRole = roleRepository.findByName(Roles.USER)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Role USER not found"));

        UserRole userRole = new UserRole();
        userRole.setRole(buyerRole);
        userRole.getId().setRoleId(buyerRole.getId());
        user.addUserRole(userRole);

        user = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        userProfileRepository.save(profile);

        sendVerificationLink(user);
        log.info("[Auth] Registered user: {}", user.getEmail());

        return buildRegisterResponse(user, profile);
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

        if (!UserStatuses.ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw new AppException(AuthErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);

        log.info("[Auth] User logged in: {}", user.getEmail());
        return buildAuthResponse(user, roles, profile);
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

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AppException(AuthErrorCode.EMAIL_NOT_VERIFIED,
                    "Vui lòng xác minh email trước khi sử dụng phiên đăng nhập");
        }

        if (!UserStatuses.ACTIVE.equalsIgnoreCase(user.getStatus())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new AppException(AuthErrorCode.ACCOUNT_DISABLED, "Tài khoản đã bị vô hiệu hóa");
        }

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);

        String newAccessToken = jwtService.generateAccessToken(user.getId(), roles);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(stored.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
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
    public void sendVerificationEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                log.debug("[Auth] Resend verification skipped: already verified userId={}", user.getId());
                return;
            }
            sendVerificationLink(user);
        });
    }

    private void sendVerificationLink(User user) {
        String token = generateSecureToken();
        OtpToken otpToken = OtpToken.of(user.getId(), token, OtpType.EMAIL_VERIFY, OTP_TTL_MINUTES);
        otpTokenRepository.save(otpToken);
        String link = appendTokenQuery(emailVerificationPageUrl, token);
        eventPublisher.publish(new EmailVerificationMailEvent(user.getEmail(), link));
        log.info("[Auth] Verification email queued for userId={}", user.getId());
    }

    // ------------------------------------------------------------------ verifyEmail
    @Override
    @Transactional
    public AuthResponse verifyEmail(String token) {
        OtpToken otp = otpTokenRepository
                .findByCodeAndTypeAndUsedFalse(token, OtpType.EMAIL_VERIFY)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Liên kết xác minh không hợp lệ hoặc đã được sử dụng"));

        if (otp.isExpired()) {
            throw new AppException(AuthErrorCode.OTP_EXPIRED, "Liên kết xác minh đã hết hạn");
        }

        User user = userRepository.findById(otp.getUserId())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Liên kết xác minh không hợp lệ"));

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            otp.setUsed(true);
            otpTokenRepository.save(otp);
            return buildAuthResponse(user, roles, profile);
        }

        otp.setUsed(true);
        otpTokenRepository.save(otp);

        user.setEmailVerified(true);
        user.setStatus(UserStatuses.ACTIVE);
        userRepository.save(user);
        log.info("[Auth] Email verified for userId={}", user.getId());
        return buildAuthResponse(user, roles, profile);
    }

    // ------------------------------------------------------------------ forgotPassword
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Silently ignore unknown emails to prevent user enumeration
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = generateSecureToken();
            OtpToken otpToken = OtpToken.of(user.getId(), token, OtpType.PASSWORD_RESET, OTP_TTL_MINUTES);
            otpTokenRepository.save(otpToken);
            String link = appendTokenQuery(passwordResetPageUrl, token);
            eventPublisher.publish(new PasswordResetMailEvent(user.getEmail(), link));
            log.info("[Auth] Password reset email queued for userId={}", user.getId());
        });
    }

    // ------------------------------------------------------------------ resetPassword
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        OtpToken otp = otpTokenRepository
                .findByCodeAndTypeAndUsedFalse(request.getToken(), OtpType.PASSWORD_RESET)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã được sử dụng"));

        if (otp.isExpired()) {
            throw new AppException(AuthErrorCode.OTP_EXPIRED, "Liên kết đặt lại mật khẩu đã hết hạn");
        }

        User user = userRepository.findById(otp.getUserId())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

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
    private AuthResponse buildAuthResponse(User user, List<String> roles, UserProfile profile) {
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
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .roles(roles)
                .build();
    }

    private AuthResponse buildRegisterResponse(User user, UserProfile profile) {
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        return AuthResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .tokenType("Bearer")
                .expiresIn(null)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .roles(roles)
                .build();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HEX.formatHex(bytes);
    }

    private String appendTokenQuery(String pageUrl, String token) {
        String enc = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String sep = pageUrl.contains("?") ? "&" : "?";
        return pageUrl + sep + "token=" + enc;
    }
}

