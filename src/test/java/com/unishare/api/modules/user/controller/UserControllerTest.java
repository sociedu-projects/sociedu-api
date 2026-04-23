package com.unishare.api.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.config.GlobalExceptionHandler;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.exception.UserErrorCode;
import com.unishare.api.modules.user.service.UserService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests cho {@link UserController}.
 * Sử dụng MockMvc standalone (không cần Spring context) với Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        principal = new CustomUserPrincipal(
                userId,
                "testuser@gmail.com",
                "hashedpassword",
                List.of("USER"),
                List.of("VIEW_PROFILE", "UPDATE_PROFILE"),
                true
        );

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Resolver giả lập @AuthenticationPrincipal
        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return principal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // PROFILE
    // =========================================================================
    @Nested
    @DisplayName("Profile APIs")
    class ProfileTests {

        @Test
        @DisplayName("GET /profile - Thành công trả về 200 với dữ liệu profile")
        void getProfile_ReturnsSuccess() throws Exception {
            UserProfileResponse response = new UserProfileResponse();
            response.setFirstName("Test");
            response.setLastName("User");

            when(userService.getProfile(userId)).thenReturn(response);

            mockMvc.perform(get("/api/v1/users/me/profile"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.firstName").value("Test"))
                    .andExpect(jsonPath("$.data.lastName").value("User"))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("GET /profile - Profile chưa tồn tại trả về 200 với object rỗng")
        void getProfile_EmptyProfile_ReturnsSuccess() throws Exception {
            UserProfileResponse emptyResponse = new UserProfileResponse();
            emptyResponse.setUserId(userId);

            when(userService.getProfile(userId)).thenReturn(emptyResponse);

            mockMvc.perform(get("/api/v1/users/me/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.data.firstName").doesNotExist());
        }

        @Test
        @DisplayName("PUT /profile - Cập nhật thành công trả về 200 với dữ liệu mới")
        void updateProfile_ReturnsSuccess() throws Exception {
            UserProfileRequest request = new UserProfileRequest();
            request.setFirstName("Updated First");
            request.setLastName("Updated Last");
            request.setBio("Software Engineer");
            request.setLocation("Hanoi, Vietnam");

            UserProfileResponse response = new UserProfileResponse();
            response.setFirstName("Updated First");
            response.setLastName("Updated Last");
            response.setBio("Software Engineer");

            when(userService.updateProfile(eq(userId), any(UserProfileRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/users/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.firstName").value("Updated First"))
                    .andExpect(jsonPath("$.data.lastName").value("Updated Last"))
                    .andExpect(jsonPath("$.message").value("Profile updated successfully"))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("PUT /profile - firstName vượt 50 ký tự trả về 400")
        void updateProfile_FirstNameTooLong_Returns400() throws Exception {
            UserProfileRequest request = new UserProfileRequest();
            request.setFirstName("A".repeat(51));  // vi phạm @Size(max=50)

            mockMvc.perform(put("/api/v1/users/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("PUT /profile - headline vượt 150 ký tự trả về 400")
        void updateProfile_HeadlineTooLong_Returns400() throws Exception {
            UserProfileRequest request = new UserProfileRequest();
            request.setHeadline("H".repeat(151));  // vi phạm @Size(max=150)

            mockMvc.perform(put("/api/v1/users/me/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("VALIDATION_ERROR"));
        }
    }

    // =========================================================================
    // EDUCATION
    // =========================================================================
    @Nested
    @DisplayName("Education APIs")
    class EducationTests {

        @Test
        @DisplayName("GET /educations - Trả về danh sách học vấn")
        void getEducations_ReturnsSuccess() throws Exception {
            UserEducationResponse education = new UserEducationResponse();
            education.setId(UUID.randomUUID());
            education.setMajorName("Computer Science");
            education.setDegree("BSc");

            when(userService.getEducations(userId)).thenReturn(List.of(education));

            mockMvc.perform(get("/api/v1/users/me/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].majorName").value("Computer Science"))
                    .andExpect(jsonPath("$.data[0].degree").value("BSc"))
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("GET /educations - Danh sách rỗng trả về 200 với mảng rỗng")
        void getEducations_Empty_Returns200() throws Exception {
            when(userService.getEducations(userId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/users/me/educations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("POST /educations - Thêm học vấn thành công")
        void addEducation_ReturnsSuccess() throws Exception {
            UserEducationRequest request = new UserEducationRequest();
            request.setUniversityId(UUID.randomUUID());
            request.setMajorId(UUID.randomUUID());
            request.setDegree("BSc");
            request.setStartDate(LocalDate.of(2020, 9, 1));
            request.setEndDate(LocalDate.of(2024, 6, 1));

            UserEducationResponse response = new UserEducationResponse();
            response.setMajorName("IT");
            response.setDegree("BSc");

            when(userService.addEducation(eq(userId), any(UserEducationRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/educations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.majorName").value("IT"))
                    .andExpect(jsonPath("$.data.degree").value("BSc"))
                    .andExpect(jsonPath("$.message").value("Education added successfully"));
        }

        @Test
        @DisplayName("PUT /educations/{id} - Cập nhật học vấn thành công")
        void updateEducation_ReturnsSuccess() throws Exception {
            UUID educationId = UUID.randomUUID();
            UserEducationRequest request = new UserEducationRequest();
            request.setUniversityId(UUID.randomUUID());
            request.setMajorId(UUID.randomUUID());
            request.setDegree("MSc");
            request.setStartDate(LocalDate.of(2018, 9, 1));

            UserEducationResponse response = new UserEducationResponse();
            response.setMajorName("Physics");
            response.setDegree("MSc");

            when(userService.updateEducation(eq(userId), eq(educationId), any(UserEducationRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/users/me/educations/{id}", educationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.majorName").value("Physics"))
                    .andExpect(jsonPath("$.message").value("Education updated successfully"));
        }

        @Test
        @DisplayName("PUT /educations/{id} - Không tìm thấy trả về 404")
        void updateEducation_NotFound_Returns404() throws Exception {
            UUID educationId = UUID.randomUUID();
            UserEducationRequest request = new UserEducationRequest();
            request.setUniversityId(UUID.randomUUID());
            request.setMajorId(UUID.randomUUID());

            when(userService.updateEducation(eq(userId), eq(educationId), any(UserEducationRequest.class)))
                    .thenThrow(new AppException(UserErrorCode.EDUCATION_NOT_FOUND));

            mockMvc.perform(put("/api/v1/users/me/educations/{id}", educationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("EDUCATION_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("DELETE /educations/{id} - Xóa thành công trả về 200")
        void deleteEducation_ReturnsSuccess() throws Exception {
            UUID educationId = UUID.randomUUID();
            doNothing().when(userService).deleteEducation(userId, educationId);

            mockMvc.perform(delete("/api/v1/users/me/educations/{id}", educationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Education deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /educations/{id} - Không tìm thấy trả về 404")
        void deleteEducation_NotFound_Returns404() throws Exception {
            UUID educationId = UUID.randomUUID();
            doThrow(new AppException(UserErrorCode.EDUCATION_NOT_FOUND))
                    .when(userService).deleteEducation(userId, educationId);

            mockMvc.perform(delete("/api/v1/users/me/educations/{id}", educationId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("EDUCATION_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // =========================================================================
    // LANGUAGE
    // =========================================================================
    @Nested
    @DisplayName("Language APIs")
    class LanguageTests {

        @Test
        @DisplayName("GET /languages - Trả về danh sách ngôn ngữ")
        void getLanguages_ReturnsSuccess() throws Exception {
            UserLanguageResponse language = new UserLanguageResponse();
            language.setId(UUID.randomUUID());
            language.setLanguage("EN");
            language.setLevel("ADVANCED");

            when(userService.getLanguages(userId)).thenReturn(List.of(language));

            mockMvc.perform(get("/api/v1/users/me/languages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].language").value("EN"))
                    .andExpect(jsonPath("$.data[0].level").value("ADVANCED"));
        }

        @Test
        @DisplayName("GET /languages - Danh sách rỗng trả về 200 với mảng rỗng")
        void getLanguages_Empty_Returns200() throws Exception {
            when(userService.getLanguages(userId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/users/me/languages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("POST /languages - Thêm ngôn ngữ thành công")
        void addLanguage_ReturnsSuccess() throws Exception {
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("FR");
            request.setLevel("BASIC");

            UserLanguageResponse response = new UserLanguageResponse();
            response.setLanguage("FR");
            response.setLevel("BASIC");

            when(userService.addLanguage(eq(userId), any(UserLanguageRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/languages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.language").value("FR"))
                    .andExpect(jsonPath("$.message").value("Language added successfully"));
        }

        @Test
        @DisplayName("POST /languages - Thiếu language (NotBlank) trả về 400")
        void addLanguage_MissingLanguage_Returns400() throws Exception {
            UserLanguageRequest request = new UserLanguageRequest();
            // language = null → @NotBlank violation
            request.setLevel("BASIC");

            mockMvc.perform(post("/api/v1/users/me/languages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.type").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors.fields.language").exists());
        }

        @Test
        @DisplayName("POST /languages - Thiếu level (NotBlank) trả về 400")
        void addLanguage_MissingLevel_Returns400() throws Exception {
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("EN");
            // level = null → @NotBlank violation

            mockMvc.perform(post("/api/v1/users/me/languages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.level").exists());
        }

        @Test
        @DisplayName("PUT /languages/{id} - Cập nhật thành công")
        void updateLanguage_ReturnsSuccess() throws Exception {
            UUID languageId = UUID.randomUUID();
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("FR");
            request.setLevel("FLUENT");

            UserLanguageResponse response = new UserLanguageResponse();
            response.setLanguage("FR");
            response.setLevel("FLUENT");

            when(userService.updateLanguage(eq(userId), eq(languageId), any(UserLanguageRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/users/me/languages/{id}", languageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.level").value("FLUENT"))
                    .andExpect(jsonPath("$.message").value("Language updated successfully"));
        }

        @Test
        @DisplayName("PUT /languages/{id} - Không tìm thấy trả về 404")
        void updateLanguage_NotFound_Returns404() throws Exception {
            UUID languageId = UUID.randomUUID();
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("FR");
            request.setLevel("FLUENT");

            when(userService.updateLanguage(eq(userId), eq(languageId), any(UserLanguageRequest.class)))
                    .thenThrow(new AppException(UserErrorCode.LANGUAGE_NOT_FOUND));

            mockMvc.perform(put("/api/v1/users/me/languages/{id}", languageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("LANGUAGE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("DELETE /languages/{id} - Xóa thành công trả về 200")
        void deleteLanguage_ReturnsSuccess() throws Exception {
            UUID languageId = UUID.randomUUID();
            doNothing().when(userService).deleteLanguage(userId, languageId);

            mockMvc.perform(delete("/api/v1/users/me/languages/{id}", languageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Language deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /languages/{id} - Không tìm thấy trả về 404")
        void deleteLanguage_NotFound_Returns404() throws Exception {
            UUID languageId = UUID.randomUUID();
            doThrow(new AppException(UserErrorCode.LANGUAGE_NOT_FOUND))
                    .when(userService).deleteLanguage(userId, languageId);

            mockMvc.perform(delete("/api/v1/users/me/languages/{id}", languageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("LANGUAGE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // =========================================================================
    // EXPERIENCE
    // =========================================================================
    @Nested
    @DisplayName("Experience APIs")
    class ExperienceTests {

        @Test
        @DisplayName("GET /experiences - Trả về danh sách kinh nghiệm")
        void getExperiences_ReturnsSuccess() throws Exception {
            UserExperienceResponse exp = new UserExperienceResponse();
            exp.setId(UUID.randomUUID());
            exp.setCompany("Acme Corp");
            exp.setPosition("Software Engineer");

            when(userService.getExperiences(userId)).thenReturn(List.of(exp));

            mockMvc.perform(get("/api/v1/users/me/experiences"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].company").value("Acme Corp"))
                    .andExpect(jsonPath("$.data[0].position").value("Software Engineer"));
        }

        @Test
        @DisplayName("GET /experiences - Danh sách rỗng trả về 200")
        void getExperiences_Empty_Returns200() throws Exception {
            when(userService.getExperiences(userId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/users/me/experiences"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("POST /experiences - Thêm kinh nghiệm thành công")
        void addExperience_ReturnsSuccess() throws Exception {
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("Acme Corp");
            request.setPosition("Developer");
            request.setStartDate(LocalDate.of(2021, 1, 1));

            UserExperienceResponse response = new UserExperienceResponse();
            response.setCompany("Acme Corp");
            response.setPosition("Developer");

            when(userService.addExperience(eq(userId), any(UserExperienceRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/experiences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.company").value("Acme Corp"))
                    .andExpect(jsonPath("$.message").value("Experience added successfully"));
        }

        @Test
        @DisplayName("POST /experiences - Thiếu company (NotBlank) trả về 400")
        void addExperience_MissingCompany_Returns400() throws Exception {
            UserExperienceRequest request = new UserExperienceRequest();
            // company = null → @NotBlank violation
            request.setPosition("Developer");
            request.setStartDate(LocalDate.of(2021, 1, 1));

            mockMvc.perform(post("/api/v1/users/me/experiences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.company").exists());
        }

        @Test
        @DisplayName("POST /experiences - Thiếu startDate (NotNull) trả về 400")
        void addExperience_MissingStartDate_Returns400() throws Exception {
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("Acme Corp");
            request.setPosition("Developer");
            // startDate = null → @NotNull violation

            mockMvc.perform(post("/api/v1/users/me/experiences")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.startDate").exists());
        }

        @Test
        @DisplayName("PUT /experiences/{id} - Cập nhật thành công")
        void updateExperience_ReturnsSuccess() throws Exception {
            UUID expId = UUID.randomUUID();
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("Global Corp");
            request.setPosition("Senior Developer");
            request.setStartDate(LocalDate.of(2022, 3, 1));

            UserExperienceResponse response = new UserExperienceResponse();
            response.setCompany("Global Corp");
            response.setPosition("Senior Developer");

            when(userService.updateExperience(eq(userId), eq(expId), any(UserExperienceRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/users/me/experiences/{id}", expId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.company").value("Global Corp"))
                    .andExpect(jsonPath("$.message").value("Experience updated successfully"));
        }

        @Test
        @DisplayName("PUT /experiences/{id} - Không tìm thấy trả về 404")
        void updateExperience_NotFound_Returns404() throws Exception {
            UUID expId = UUID.randomUUID();
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("Global Corp");
            request.setPosition("Senior Developer");
            request.setStartDate(LocalDate.of(2022, 3, 1));

            when(userService.updateExperience(eq(userId), eq(expId), any(UserExperienceRequest.class)))
                    .thenThrow(new AppException(UserErrorCode.EXPERIENCE_NOT_FOUND));

            mockMvc.perform(put("/api/v1/users/me/experiences/{id}", expId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("EXPERIENCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("DELETE /experiences/{id} - Xóa thành công trả về 200")
        void deleteExperience_ReturnsSuccess() throws Exception {
            UUID expId = UUID.randomUUID();
            doNothing().when(userService).deleteExperience(userId, expId);

            mockMvc.perform(delete("/api/v1/users/me/experiences/{id}", expId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Experience deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /experiences/{id} - Không tìm thấy trả về 404")
        void deleteExperience_NotFound_Returns404() throws Exception {
            UUID expId = UUID.randomUUID();
            doThrow(new AppException(UserErrorCode.EXPERIENCE_NOT_FOUND))
                    .when(userService).deleteExperience(userId, expId);

            mockMvc.perform(delete("/api/v1/users/me/experiences/{id}", expId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("EXPERIENCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    // =========================================================================
    // CERTIFICATE
    // =========================================================================
    @Nested
    @DisplayName("Certificate APIs")
    class CertificateTests {

        @Test
        @DisplayName("GET /certificates - Trả về danh sách chứng chỉ")
        void getCertificates_ReturnsSuccess() throws Exception {
            UserCertificateResponse cert = new UserCertificateResponse();
            cert.setId(UUID.randomUUID());
            cert.setName("AWS Certified");
            cert.setOrganization("Amazon");

            when(userService.getCertificates(userId)).thenReturn(List.of(cert));

            mockMvc.perform(get("/api/v1/users/me/certificates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].name").value("AWS Certified"))
                    .andExpect(jsonPath("$.data[0].organization").value("Amazon"));
        }

        @Test
        @DisplayName("GET /certificates - Danh sách rỗng trả về 200")
        void getCertificates_Empty_Returns200() throws Exception {
            when(userService.getCertificates(userId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/users/me/certificates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("POST /certificates - Thêm chứng chỉ thành công")
        void addCertificate_ReturnsSuccess() throws Exception {
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("AWS Certified");
            request.setOrganization("Amazon");
            request.setIssueDate(LocalDate.of(2023, 6, 1));

            UserCertificateResponse response = new UserCertificateResponse();
            response.setName("AWS Certified");
            response.setOrganization("Amazon");

            when(userService.addCertificate(eq(userId), any(UserCertificateRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/me/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("AWS Certified"))
                    .andExpect(jsonPath("$.data.organization").value("Amazon"))
                    .andExpect(jsonPath("$.message").value("Certificate added successfully"));
        }

        @Test
        @DisplayName("POST /certificates - Thiếu name (NotBlank) trả về 400")
        void addCertificate_MissingName_Returns400() throws Exception {
            UserCertificateRequest request = new UserCertificateRequest();
            // name = null → @NotBlank violation
            request.setOrganization("Amazon");
            request.setIssueDate(LocalDate.of(2023, 6, 1));

            mockMvc.perform(post("/api/v1/users/me/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.name").exists());
        }

        @Test
        @DisplayName("POST /certificates - Thiếu organization (NotBlank) trả về 400")
        void addCertificate_MissingOrganization_Returns400() throws Exception {
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("AWS Certified");
            // organization = null → @NotBlank violation
            request.setIssueDate(LocalDate.of(2023, 6, 1));

            mockMvc.perform(post("/api/v1/users/me/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.organization").exists());
        }

        @Test
        @DisplayName("POST /certificates - Thiếu issueDate (NotNull) trả về 400")
        void addCertificate_MissingIssueDate_Returns400() throws Exception {
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("AWS Certified");
            request.setOrganization("Amazon");
            // issueDate = null → @NotNull violation

            mockMvc.perform(post("/api/v1/users/me/certificates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.fields.issueDate").exists());
        }

        @Test
        @DisplayName("PUT /certificates/{id} - Cập nhật thành công")
        void updateCertificate_ReturnsSuccess() throws Exception {
            UUID certId = UUID.randomUUID();
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("GCP Certified");
            request.setOrganization("Google");
            request.setIssueDate(LocalDate.of(2024, 1, 15));

            UserCertificateResponse response = new UserCertificateResponse();
            response.setName("GCP Certified");
            response.setOrganization("Google");

            when(userService.updateCertificate(eq(userId), eq(certId), any(UserCertificateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/users/me/certificates/{id}", certId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("GCP Certified"))
                    .andExpect(jsonPath("$.message").value("Certificate updated successfully"));
        }

        @Test
        @DisplayName("PUT /certificates/{id} - Không tìm thấy trả về 404")
        void updateCertificate_NotFound_Returns404() throws Exception {
            UUID certId = UUID.randomUUID();
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("GCP Certified");
            request.setOrganization("Google");
            request.setIssueDate(LocalDate.of(2024, 1, 15));

            when(userService.updateCertificate(eq(userId), eq(certId), any(UserCertificateRequest.class)))
                    .thenThrow(new AppException(UserErrorCode.CERTIFICATE_NOT_FOUND));

            mockMvc.perform(put("/api/v1/users/me/certificates/{id}", certId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("CERTIFICATE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("DELETE /certificates/{id} - Xóa thành công trả về 200")
        void deleteCertificate_ReturnsSuccess() throws Exception {
            UUID certId = UUID.randomUUID();
            doNothing().when(userService).deleteCertificate(userId, certId);

            mockMvc.perform(delete("/api/v1/users/me/certificates/{id}", certId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Certificate deleted successfully"));
        }

        @Test
        @DisplayName("DELETE /certificates/{id} - Không tìm thấy trả về 404")
        void deleteCertificate_NotFound_Returns404() throws Exception {
            UUID certId = UUID.randomUUID();
            doThrow(new AppException(UserErrorCode.CERTIFICATE_NOT_FOUND))
                    .when(userService).deleteCertificate(userId, certId);

            mockMvc.perform(delete("/api/v1/users/me/certificates/{id}", certId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors.type").value("CERTIFICATE_NOT_FOUND"))
                    .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }
}
