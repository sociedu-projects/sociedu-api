package com.unishare.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.auth.dto.request.SendPhoneOtpRequest;
import com.unishare.api.modules.auth.dto.request.VerifyPhoneOtpRequest;
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
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests cho Flow A — xác thực số điện thoại (authenticated
 * endpoints).
 * Dùng standalone MockMvc + custom ArgumentResolver để inject
 * CustomUserPrincipal.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerPhoneOtpTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Inject a fake CustomUserPrincipal for @AuthenticationPrincipal resolution
        CustomUserPrincipal fakePrincipal = new CustomUserPrincipal(
                userId, "test@example.com", "",
                List.of("USER"), List.of(), true);

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(CustomUserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return fakePrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // POST /api/v1/auth/phone/send-otp
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/phone/send-otp")
    class SendPhoneOtpTests {

        @Test
        @DisplayName("Thành công — 200")
        void sendPhoneOtp_success_200() throws Exception {
            SendPhoneOtpRequest req = new SendPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");

            doNothing().when(authService).sendPhoneVerificationOtp(any(UUID.class), any(SendPhoneOtpRequest.class));

            mockMvc.perform(post("/api/v1/auth/phone/send-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Phone trống — 400")
        void sendPhoneOtp_blankPhone_400() throws Exception {
            SendPhoneOtpRequest req = new SendPhoneOtpRequest();
            req.setPhoneNumber("");

            mockMvc.perform(post("/api/v1/auth/phone/send-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Phone format sai — 400")
        void sendPhoneOtp_invalidFormat_400() throws Exception {
            SendPhoneOtpRequest req = new SendPhoneOtpRequest();
            req.setPhoneNumber("abc123");

            mockMvc.perform(post("/api/v1/auth/phone/send-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Phone đã verified — 400 PHONE_ALREADY_VERIFIED")
        void sendPhoneOtp_alreadyVerified_400() throws Exception {
            SendPhoneOtpRequest req = new SendPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");

            doThrow(new AppException(AuthErrorCode.PHONE_ALREADY_VERIFIED))
                    .when(authService).sendPhoneVerificationOtp(any(), any());

            mockMvc.perform(post("/api/v1/auth/phone/send-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("PHONE_ALREADY_VERIFIED"));
        }

        @Test
        @DisplayName("Rate limited — 429 OTP_RATE_LIMITED")
        void sendPhoneOtp_rateLimited_429() throws Exception {
            SendPhoneOtpRequest req = new SendPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");

            doThrow(new AppException(AuthErrorCode.OTP_RATE_LIMITED))
                    .when(authService).sendPhoneVerificationOtp(any(), any());

            mockMvc.perform(post("/api/v1/auth/phone/send-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.errors.type").value("OTP_RATE_LIMITED"));
        }
    }

    // =========================================================================
    // POST /api/v1/auth/phone/verify-otp
    // =========================================================================
    @Nested
    @DisplayName("POST /api/v1/auth/phone/verify-otp")
    class VerifyPhoneOtpTests {

        @Test
        @DisplayName("Thành công — 200")
        void verifyPhoneOtp_success_200() throws Exception {
            VerifyPhoneOtpRequest req = new VerifyPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");
            req.setOtpCode("123456");

            doNothing().when(authService).verifyPhoneOtp(any(UUID.class), any(VerifyPhoneOtpRequest.class));

            mockMvc.perform(post("/api/v1/auth/phone/verify-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("OTP trống — 400")
        void verifyPhoneOtp_blankOtp_400() throws Exception {
            VerifyPhoneOtpRequest req = new VerifyPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");
            req.setOtpCode("");

            mockMvc.perform(post("/api/v1/auth/phone/verify-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("OTP sai độ dài — 400")
        void verifyPhoneOtp_otpWrongSize_400() throws Exception {
            VerifyPhoneOtpRequest req = new VerifyPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");
            req.setOtpCode("12345"); // chỉ 5 chữ số

            mockMvc.perform(post("/api/v1/auth/phone/verify-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("OTP không hợp lệ — 400 INVALID_OTP")
        void verifyPhoneOtp_invalidOtp_400() throws Exception {
            VerifyPhoneOtpRequest req = new VerifyPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");
            req.setOtpCode("000000");

            doThrow(new AppException(AuthErrorCode.INVALID_OTP))
                    .when(authService).verifyPhoneOtp(any(), any());

            mockMvc.perform(post("/api/v1/auth/phone/verify-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("INVALID_OTP"));
        }

        @Test
        @DisplayName("OTP hết hạn — 400 OTP_EXPIRED")
        void verifyPhoneOtp_expired_400() throws Exception {
            VerifyPhoneOtpRequest req = new VerifyPhoneOtpRequest();
            req.setPhoneNumber("+84912345678");
            req.setOtpCode("123456");

            doThrow(new AppException(AuthErrorCode.OTP_EXPIRED))
                    .when(authService).verifyPhoneOtp(any(), any());

            mockMvc.perform(post("/api/v1/auth/phone/verify-otp")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("OTP_EXPIRED"));
        }
    }
}
