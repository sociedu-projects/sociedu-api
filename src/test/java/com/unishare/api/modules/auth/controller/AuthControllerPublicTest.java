package com.unishare.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.auth.dto.request.*;
import com.unishare.api.modules.auth.dto.response.AuthResponse;
import com.unishare.api.modules.auth.exception.AuthErrorCode;
import com.unishare.api.modules.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests cho {@link AuthController} — các endpoint PUBLIC
 * (register, login, refresh, logout-validation, verify-email, resend, forgot/reset password).
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerPublicTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse sampleAuthResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleAuthResponse = AuthResponse.builder()
                .accessToken("access-jwt")
                .refreshToken("refresh-jwt")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(UUID.randomUUID())
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(List.of("USER"))
                .build();
    }

    // =========================================================================
    // REGISTER
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Đăng ký thành công — 201")
        void register_Success_Returns201() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("new@example.com");
            req.setPassword("password123");
            req.setFirstName("Huy");
            req.setLastName("Trinh");

            when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.accessToken").value("access-jwt"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-jwt"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("Email trống — 400 VALIDATION_ERROR")
        void register_BlankEmail_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("");
            req.setPassword("password123");
            req.setFirstName("A");
            req.setLastName("B");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Email không hợp lệ — 400")
        void register_InvalidEmail_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("not-an-email");
            req.setPassword("password123");
            req.setFirstName("A");
            req.setLastName("B");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Password < 8 ký tự — 400")
        void register_ShortPassword_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            req.setPassword("short");
            req.setFirstName("A");
            req.setLastName("B");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.password").exists());
        }

        @Test
        @DisplayName("Thiếu firstName — 400")
        void register_BlankFirstName_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFirstName("");
            req.setLastName("B");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.firstName").exists());
        }

        @Test
        @DisplayName("Thiếu lastName — 400")
        void register_BlankLastName_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFirstName("A");
            req.setLastName("");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.lastName").exists());
        }

        @Test
        @DisplayName("firstName > 50 ký tự — 400")
        void register_FirstNameTooLong_Returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFirstName("A".repeat(51));
            req.setLastName("B");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.firstName").exists());
        }

        @Test
        @DisplayName("Email đã tồn tại — 409 EMAIL_ALREADY_EXISTS")
        void register_DuplicateEmail_Returns409() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("dup@example.com");
            req.setPassword("password123");
            req.setFirstName("A");
            req.setLastName("B");

            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new AppException(AuthErrorCode.EMAIL_ALREADY_EXISTS));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errors.type").value("EMAIL_ALREADY_EXISTS"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // =========================================================================
    // LOGIN
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Đăng nhập thành công — 200")
        void login_Success_Returns200() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("user@example.com");
            req.setPassword("password123");

            when(authService.login(any(LoginRequest.class), anyString(), any()))
                    .thenReturn(sampleAuthResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.email").value("user@example.com"))
                    .andExpect(jsonPath("$.message").value("Đăng nhập thành công."))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("Email trống — 400")
        void login_BlankEmail_Returns400() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("");
            req.setPassword("password123");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Password trống — 400")
        void login_BlankPassword_Returns400() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("user@example.com");
            req.setPassword("");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.password").exists());
        }

        @Test
        @DisplayName("Sai mật khẩu — 401 INVALID_CREDENTIALS")
        void login_InvalidCredentials_Returns401() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("user@example.com");
            req.setPassword("wrong-password");

            when(authService.login(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.INVALID_CREDENTIALS));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("Email chưa xác minh — 403 EMAIL_NOT_VERIFIED")
        void login_EmailNotVerified_Returns403() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("user@example.com");
            req.setPassword("password123");

            when(authService.login(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.EMAIL_NOT_VERIFIED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors.type").value("EMAIL_NOT_VERIFIED"));
        }

        @Test
        @DisplayName("Tài khoản bị vô hiệu — 403 ACCOUNT_DISABLED")
        void login_AccountDisabled_Returns403() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setEmail("user@example.com");
            req.setPassword("password123");

            when(authService.login(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.ACCOUNT_DISABLED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors.type").value("ACCOUNT_DISABLED"));
        }
    }

    // =========================================================================
    // REFRESH TOKEN
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshTests {

        @Test
        @DisplayName("Làm mới token thành công — 200")
        void refresh_Success_Returns200() throws Exception {
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken("valid-refresh-token");

            when(authService.refreshToken(any(RefreshTokenRequest.class), anyString(), any()))
                    .thenReturn(sampleAuthResponse);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists());
        }

        @Test
        @DisplayName("Refresh token trống — 400")
        void refresh_BlankToken_Returns400() throws Exception {
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken("");

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.refreshToken").exists());
        }

        @Test
        @DisplayName("Token không hợp lệ — 401 INVALID_TOKEN")
        void refresh_InvalidToken_Returns401() throws Exception {
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken("bad-token");

            when(authService.refreshToken(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.INVALID_TOKEN));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("Token hết hạn — 401 TOKEN_EXPIRED")
        void refresh_TokenExpired_Returns401() throws Exception {
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken("expired-token");

            when(authService.refreshToken(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.TOKEN_EXPIRED));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("TOKEN_EXPIRED"));
        }

        @Test
        @DisplayName("Token bị tái sử dụng — 401 REFRESH_TOKEN_REUSED")
        void refresh_TokenReused_Returns401() throws Exception {
            RefreshTokenRequest req = new RefreshTokenRequest();
            req.setRefreshToken("reused-token");

            when(authService.refreshToken(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.REFRESH_TOKEN_REUSED));

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("REFRESH_TOKEN_REUSED"));
        }
    }

    // =========================================================================
    // VERIFY EMAIL
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/verify-email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Xác minh email thành công — 200")
        void verifyEmail_Success_Returns200() throws Exception {
            VerifyEmailRequest req = new VerifyEmailRequest();
            req.setToken("valid-verify-token");

            when(authService.verifyEmail("valid-verify-token")).thenReturn(sampleAuthResponse);

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Xác minh email thành công."))
                    .andExpect(jsonPath("$.data.accessToken").exists());
        }

        @Test
        @DisplayName("Token trống — 400")
        void verifyEmail_BlankToken_Returns400() throws Exception {
            VerifyEmailRequest req = new VerifyEmailRequest();
            req.setToken("");

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.token").exists());
        }

        @Test
        @DisplayName("Token không hợp lệ — 401")
        void verifyEmail_InvalidToken_Returns401() throws Exception {
            VerifyEmailRequest req = new VerifyEmailRequest();
            req.setToken("invalid-token");

            when(authService.verifyEmail("invalid-token"))
                    .thenThrow(new AppException(AuthErrorCode.INVALID_TOKEN));

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_TOKEN"));
        }

        @Test
        @DisplayName("Token hết hạn — 401")
        void verifyEmail_TokenExpired_Returns401() throws Exception {
            VerifyEmailRequest req = new VerifyEmailRequest();
            req.setToken("expired-token");

            when(authService.verifyEmail("expired-token"))
                    .thenThrow(new AppException(AuthErrorCode.TOKEN_EXPIRED));

            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("TOKEN_EXPIRED"));
        }
    }

    // =========================================================================
    // RESEND VERIFICATION
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/resend-verification")
    class ResendVerificationTests {

        @Test
        @DisplayName("Gửi lại email xác minh — 200")
        void resend_Success_Returns200() throws Exception {
            ResendVerificationRequest req = new ResendVerificationRequest();
            req.setEmail("user@example.com");

            doNothing().when(authService).sendVerificationEmail("user@example.com");

            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("Email trống — 400")
        void resend_BlankEmail_Returns400() throws Exception {
            ResendVerificationRequest req = new ResendVerificationRequest();
            req.setEmail("");

            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Email không hợp lệ — 400")
        void resend_InvalidEmail_Returns400() throws Exception {
            ResendVerificationRequest req = new ResendVerificationRequest();
            req.setEmail("not-email");

            mockMvc.perform(post("/api/v1/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }
    }

    // =========================================================================
    // FORGOT PASSWORD
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Gửi link reset password — 200")
        void forgot_Success_Returns200() throws Exception {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("user@example.com");

            doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("Email trống — 400")
        void forgot_BlankEmail_Returns400() throws Exception {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("");

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Email không hợp lệ — 400")
        void forgot_InvalidEmail_Returns400() throws Exception {
            ForgotPasswordRequest req = new ForgotPasswordRequest();
            req.setEmail("bad");

            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }
    }

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Đặt lại mật khẩu thành công — 200")
        void reset_Success_Returns200() throws Exception {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("valid-reset-token");
            req.setNewPassword("newPassword123");

            doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Đặt lại mật khẩu thành công."))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("Token trống — 400")
        void reset_BlankToken_Returns400() throws Exception {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("");
            req.setNewPassword("newPassword123");

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.token").exists());
        }

        @Test
        @DisplayName("Mật khẩu mới < 8 ký tự — 400")
        void reset_ShortPassword_Returns400() throws Exception {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("valid-token");
            req.setNewPassword("short");

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.newPassword").exists());
        }

        @Test
        @DisplayName("Token không hợp lệ — 401")
        void reset_InvalidToken_Returns401() throws Exception {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setToken("bad-token");
            req.setNewPassword("newPassword123");

            doThrow(new AppException(AuthErrorCode.INVALID_TOKEN))
                    .when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_TOKEN"));
        }
    }
}
