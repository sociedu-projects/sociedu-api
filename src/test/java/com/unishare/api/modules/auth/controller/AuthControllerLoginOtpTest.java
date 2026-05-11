package com.unishare.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.modules.auth.dto.request.LoginOtpRequest;
import com.unishare.api.modules.auth.dto.request.SendLoginOtpRequest;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests cho Flow C — đăng nhập bằng email OTP (public endpoints).
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerLoginOtpTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

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
    // POST /api/v1/auth/otp/send
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/otp/send")
    class SendLoginOtpTests {

        @Test
        @DisplayName("Thành công — 200")
        void sendLoginOtp_success_200() throws Exception {
            SendLoginOtpRequest req = new SendLoginOtpRequest();
            req.setEmail("user@example.com");

            doNothing().when(authService).sendLoginOtp(any(SendLoginOtpRequest.class));

            mockMvc.perform(post("/api/v1/auth/otp/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.message").value("Nếu email hợp lệ, mã OTP đã được gửi."));
        }

        @Test
        @DisplayName("Email trống — 400")
        void sendLoginOtp_blankEmail_400() throws Exception {
            SendLoginOtpRequest req = new SendLoginOtpRequest();
            req.setEmail("");

            mockMvc.perform(post("/api/v1/auth/otp/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Email không hợp lệ — 400")
        void sendLoginOtp_invalidEmail_400() throws Exception {
            SendLoginOtpRequest req = new SendLoginOtpRequest();
            req.setEmail("not-an-email");

            mockMvc.perform(post("/api/v1/auth/otp/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("Rate limited — 429")
        void sendLoginOtp_rateLimited_429() throws Exception {
            SendLoginOtpRequest req = new SendLoginOtpRequest();
            req.setEmail("user@example.com");

            doThrow(new AppException(AuthErrorCode.OTP_RATE_LIMITED))
                    .when(authService).sendLoginOtp(any());

            mockMvc.perform(post("/api/v1/auth/otp/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.errors.type").value("OTP_RATE_LIMITED"));
        }
    }

    // =========================================================================
    // POST /api/v1/auth/otp/login
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/otp/login")
    class LoginOtpTests {

        @Test
        @DisplayName("Thành công — 200 + AuthResponse")
        void loginOtp_success_200() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("123456");

            when(authService.loginWithOtp(any(LoginOtpRequest.class), anyString(), any()))
                    .thenReturn(sampleAuthResponse);

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("access-jwt"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-jwt"))
                    .andExpect(jsonPath("$.message").value("Đăng nhập thành công."));
        }

        @Test
        @DisplayName("OTP trống — 400")
        void loginOtp_blankOtp_400() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("");

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.otpCode").exists());
        }

        @Test
        @DisplayName("OTP sai độ dài — 400")
        void loginOtp_otpWrongSize_400() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("1234567"); // 7 digits

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.otpCode").exists());
        }

        @Test
        @DisplayName("Email trống — 400")
        void loginOtp_blankEmail_400() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("");
            req.setOtpCode("123456");

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.email").exists());
        }

        @Test
        @DisplayName("OTP không hợp lệ — 400 INVALID_OTP")
        void loginOtp_invalidOtp_400() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("000000");

            when(authService.loginWithOtp(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.INVALID_OTP));

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_OTP"));
        }

        @Test
        @DisplayName("OTP hết hạn — 400 OTP_EXPIRED")
        void loginOtp_expired_400() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("123456");

            when(authService.loginWithOtp(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.OTP_EXPIRED));

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("OTP_EXPIRED"));
        }

        @Test
        @DisplayName("Account disabled — 403 ACCOUNT_DISABLED")
        void loginOtp_accountDisabled_403() throws Exception {
            LoginOtpRequest req = new LoginOtpRequest();
            req.setEmail("user@example.com");
            req.setOtpCode("123456");

            when(authService.loginWithOtp(any(), anyString(), any()))
                    .thenThrow(new AppException(AuthErrorCode.ACCOUNT_DISABLED));

            mockMvc.perform(post("/api/v1/auth/otp/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors.type").value("ACCOUNT_DISABLED"));
        }
    }
}
