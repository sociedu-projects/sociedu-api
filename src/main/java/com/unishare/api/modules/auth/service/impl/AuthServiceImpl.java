package com.unishare.api.modules.auth.service.impl;

import com.unishare.api.common.constants.Roles;
import com.unishare.api.common.constants.UserStatuses;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.event.DomainEventPublisher;
import com.unishare.api.common.event.EmailVerificationMailEvent;
import com.unishare.api.common.event.PasswordResetMailEvent;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.dto.response.MeResponse;
import com.unishare.api.modules.auth.dto.response.SessionResponse;
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
import java.util.UUID;

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
    private final CapabilityRepository capabilityRepository;
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
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
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
        return buildAuthResponse(user, roles, profile, ipAddress, userAgent);
    }

    // ------------------------------------------------------------------ refreshToken
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshToken stored = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_TOKEN, "Refresh token không hợp lệ"));

        // Reuse-detection: token đã bị replace (rotation đã thực hiện) hoặc đã revoke
        // mà vẫn được gửi lên => nghi bị đánh cắp, revoke toàn bộ phiên của user.
        if (Boolean.TRUE.equals(stored.getRevoked()) || stored.getReplacedById() != null) {
            log.warn("[Auth] Refresh token reuse detected for userId={}, revoking all sessions",
                    stored.getUserId());
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            throw new AppException(AuthErrorCode.REFRESH_TOKEN_REUSED,
                    "Phát hiện sử dụng lại refresh token, toàn bộ phiên đã bị thu hồi");
        }

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

        // Rotation: issue a new refresh token and mark the old one as replaced.
        String newRawRefreshToken = jwtService.generateRefreshToken();
        RefreshToken newToken = RefreshToken.of(
                user.getId(),
                newRawRefreshToken,
                jwtService.getRefreshTokenExpiry(),
                ipAddress != null ? ipAddress : stored.getIpAddress(),
                userAgent != null ? userAgent : stored.getUserAgent(),
                parseDeviceInfo(userAgent != null ? userAgent : stored.getUserAgent()));
        newToken = refreshTokenRepository.save(newToken);

        stored.setReplacedById(newToken.getId());
        stored.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(stored);

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
        String newAccessToken = jwtService.generateAccessToken(user.getId(), roles);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
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
    public void verifyEmail(String token) {
        OtpToken otp = otpTokenRepository
                .findByCodeAndTypeAndUsedFalse(token, OtpType.EMAIL_VERIFY)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Liên kết xác minh không hợp lệ hoặc đã được sử dụng"));

        if (otp.isExpired()) {
            throw new AppException(AuthErrorCode.OTP_EXPIRED, "Liên kết xác minh đã hết hạn");
        }

        User user = userRepository.findById(otp.getUserId())
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_OTP, "Liên kết xác minh không hợp lệ"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            otp.setUsed(true);
            otpTokenRepository.save(otp);
            return;
        }

        otp.setUsed(true);
        otpTokenRepository.save(otp);

        user.setEmailVerified(true);
        user.setStatus(UserStatuses.ACTIVE);
        userRepository.save(user);
        log.info("[Auth] Email verified for userId={}", user.getId());
    }

    // ------------------------------------------------------------------ forgotPassword
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
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

        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("[Auth] Password reset for userId={}", user.getId());
    }

    // ------------------------------------------------------------------ getMe
    @Override
    @Transactional(readOnly = true)
    public MeResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        List<String> capabilities = capabilityRepository.findCapabilityNamesByUserId(userId);

        String firstName = profile != null ? profile.getFirstName() : null;
        String lastName = profile != null ? profile.getLastName() : null;
        String fullName = joinName(firstName, lastName);

        return MeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .status(user.getStatus())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName)
                .headline(profile != null ? profile.getHeadline() : null)
                .avatarUrl(null)
                .roles(roles)
                .capabilities(capabilities)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ------------------------------------------------------------------ changePassword
    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request, String currentRefreshToken) {
        UserCredential credential = userCredentialRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPasswordHash())) {
            throw new AppException(AuthErrorCode.INVALID_CURRENT_PASSWORD,
                    "Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), credential.getPasswordHash())) {
            throw new AppException(AuthErrorCode.INVALID_CURRENT_PASSWORD,
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credential.setUpdatedAt(Instant.now());
        userCredentialRepository.save(credential);

        // Thu hồi mọi phiên khác (giữ lại phiên hiện tại nếu có).
        List<RefreshToken> active = refreshTokenRepository.findActiveSessionsByUserId(userId, Instant.now());
        for (RefreshToken rt : active) {
            if (currentRefreshToken != null && currentRefreshToken.equals(rt.getToken())) {
                continue;
            }
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        }
        log.info("[Auth] Password changed for userId={}, kept current session", userId);
    }

    // ------------------------------------------------------------------ listSessions
    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(UUID userId, String currentRefreshToken) {
        return refreshTokenRepository.findActiveSessionsByUserId(userId, Instant.now()).stream()
                .map(rt -> SessionResponse.builder()
                        .id(rt.getId())
                        .deviceInfo(rt.getDeviceInfo())
                        .ipAddress(rt.getIpAddress())
                        .userAgent(rt.getUserAgent())
                        .createdAt(rt.getCreatedAt())
                        .lastUsedAt(rt.getLastUsedAt())
                        .expiresAt(rt.getExpiresAt())
                        .current(currentRefreshToken != null && currentRefreshToken.equals(rt.getToken()))
                        .build())
                .toList();
    }

    // ------------------------------------------------------------------ revokeSession
    @Override
    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        RefreshToken rt = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(AuthErrorCode.SESSION_NOT_FOUND,
                        "Phiên đăng nhập không tồn tại"));
        if (!rt.getUserId().equals(userId)) {
            throw new AppException(AuthErrorCode.ACCESS_DENIED,
                    "Không thể thao tác trên phiên của người khác");
        }
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        log.info("[Auth] Session {} revoked by owner userId={}", sessionId, userId);
    }

    // ------------------------------------------------------------------ revokeAllSessions
    @Override
    @Transactional
    public void revokeAllSessions(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("[Auth] All sessions revoked for userId={}", userId);
    }

    // ------------------------------------------------------------------ helpers
    private AuthResponse buildAuthResponse(User user, List<String> roles, UserProfile profile,
                                           String ipAddress, String userAgent) {
        String accessToken = jwtService.generateAccessToken(user.getId(), roles);
        String rawRefreshToken = jwtService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.of(
                user.getId(),
                rawRefreshToken,
                jwtService.getRefreshTokenExpiry(),
                ipAddress,
                userAgent,
                parseDeviceInfo(userAgent));
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

    /** Quick-and-dirty User-Agent parsing into a short human-readable label. */
    private String parseDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        String ua = userAgent.toLowerCase();
        String os = "Unknown OS";
        if (ua.contains("windows")) os = "Windows";
        else if (ua.contains("mac os") || ua.contains("macintosh")) os = "macOS";
        else if (ua.contains("android")) os = "Android";
        else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) os = "iOS";
        else if (ua.contains("linux")) os = "Linux";

        String browser = "Unknown browser";
        if (ua.contains("edg/")) browser = "Edge";
        else if (ua.contains("chrome") && !ua.contains("edg/")) browser = "Chrome";
        else if (ua.contains("firefox")) browser = "Firefox";
        else if (ua.contains("safari") && !ua.contains("chrome")) browser = "Safari";

        return browser + " on " + os;
    }

    private String joinName(String first, String last) {
        String f = first != null ? first.trim() : "";
        String l = last != null ? last.trim() : "";
        String joined = (l + " " + f).trim();
        return joined.isEmpty() ? null : joined;
    }
}
