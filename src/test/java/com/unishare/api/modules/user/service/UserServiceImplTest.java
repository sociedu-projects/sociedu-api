package com.unishare.api.modules.user.service;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.*;
import com.unishare.api.modules.user.exception.UserErrorCode;
import com.unishare.api.modules.user.mapper.UserMapper;
import com.unishare.api.modules.user.repository.*;
import com.unishare.api.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho {@link UserServiceImpl} - kiểm thử business logic.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserProfileRepository profileRepository;
    @Mock private UserEducationRepository educationRepository;
    @Mock private UserLanguageRepository languageRepository;
    @Mock private UserExperienceRepository experienceRepository;
    @Mock private UserCertificateRepository certificateRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // =========================================================================
    // PROFILE
    // =========================================================================
    @Nested
    @DisplayName("UserService - Profile")
    class ProfileServiceTests {

        @Test
        @DisplayName("getProfile - Profile tồn tại → trả về response đã map")
        void getProfile_Exists_ReturnsMappedResponse() {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setFirstName("Nguyen");

            UserProfileResponse response = new UserProfileResponse();
            response.setUserId(userId);
            response.setFirstName("Nguyen");

            when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
            when(userMapper.toResponse(profile)).thenReturn(response);

            UserProfileResponse result = userService.getProfile(userId);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getFirstName()).isEqualTo("Nguyen");
            verify(profileRepository).findById(userId);
            verify(userMapper).toResponse(profile);
        }

        @Test
        @DisplayName("getProfile - Profile chưa tồn tại → trả về object rỗng với userId")
        void getProfile_NotExists_ReturnsEmptyResponseWithUserId() {
            when(profileRepository.findById(userId)).thenReturn(Optional.empty());

            UserProfileResponse result = userService.getProfile(userId);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getFirstName()).isNull();
            verify(userMapper, never()).toResponse(any(UserProfile.class));
        }

        @Test
        @DisplayName("updateProfile - Profile chưa tồn tại → tạo mới và lưu")
        void updateProfile_NotExists_CreatesNew() {
            UserProfileRequest request = new UserProfileRequest();
            request.setFirstName("Van A");
            request.setBio("Lập trình viên");

            UserProfile savedProfile = new UserProfile();
            savedProfile.setUserId(userId);
            savedProfile.setFirstName("Van A");

            UserProfileResponse response = new UserProfileResponse();
            response.setUserId(userId);
            response.setFirstName("Van A");

            when(profileRepository.findById(userId)).thenReturn(Optional.empty());
            when(profileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
            when(userMapper.toResponse(savedProfile)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result.getFirstName()).isEqualTo("Van A");
            verify(profileRepository).save(any(UserProfile.class));
            verify(userMapper).updateEntity(any(UserProfile.class), eq(request));
        }

        @Test
        @DisplayName("updateProfile - Profile đã tồn tại → cập nhật và lưu")
        void updateProfile_Exists_UpdatesExisting() {
            UserProfileRequest request = new UserProfileRequest();
            request.setFirstName("Updated Name");

            UserProfile existingProfile = new UserProfile();
            existingProfile.setUserId(userId);
            existingProfile.setFirstName("Old Name");

            UserProfile savedProfile = new UserProfile();
            savedProfile.setUserId(userId);
            savedProfile.setFirstName("Updated Name");

            UserProfileResponse response = new UserProfileResponse();
            response.setFirstName("Updated Name");

            when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
            when(profileRepository.save(existingProfile)).thenReturn(savedProfile);
            when(userMapper.toResponse(savedProfile)).thenReturn(response);

            UserProfileResponse result = userService.updateProfile(userId, request);

            assertThat(result.getFirstName()).isEqualTo("Updated Name");
            verify(userMapper).updateEntity(existingProfile, request);
        }
    }

    // =========================================================================
    // EDUCATION
    // =========================================================================
    @Nested
    @DisplayName("UserService - Education")
    class EducationServiceTests {

        @Test
        @DisplayName("getEducations - Trả về danh sách đã map")
        void getEducations_ReturnsMappedList() {
            UserEducation edu1 = new UserEducation();
            edu1.setId(UUID.randomUUID());
            edu1.setUserId(userId);

            UserEducationResponse res1 = new UserEducationResponse();
            res1.setMajorName("CS");

            when(educationRepository.findByUserId(userId)).thenReturn(List.of(edu1));
            when(userMapper.toResponse(edu1)).thenReturn(res1);

            List<UserEducationResponse> result = userService.getEducations(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMajorName()).isEqualTo("CS");
        }

        @Test
        @DisplayName("getEducations - Không có dữ liệu → trả về danh sách rỗng")
        void getEducations_Empty_ReturnsEmptyList() {
            when(educationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

            List<UserEducationResponse> result = userService.getEducations(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("addEducation - Lưu và trả về response")
        void addEducation_SavesAndReturns() {
            UserEducationRequest request = new UserEducationRequest();
            request.setDegree("BSc");
            request.setStartDate(LocalDate.of(2020, 9, 1));

            UserEducation entity = new UserEducation();
            entity.setUserId(userId);

            UserEducation saved = new UserEducation();
            saved.setId(UUID.randomUUID());
            saved.setUserId(userId);
            saved.setDegree("BSc");

            UserEducationResponse response = new UserEducationResponse();
            response.setDegree("BSc");

            when(userMapper.toEntity(request, userId)).thenReturn(entity);
            when(educationRepository.save(entity)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserEducationResponse result = userService.addEducation(userId, request);

            assertThat(result.getDegree()).isEqualTo("BSc");
            verify(educationRepository).save(entity);
        }

        @Test
        @DisplayName("updateEducation - Tìm thấy và thuộc user → cập nhật thành công")
        void updateEducation_Found_Updates() {
            UUID educationId = UUID.randomUUID();
            UserEducationRequest request = new UserEducationRequest();
            request.setDegree("MSc");
            request.setStartDate(LocalDate.of(2022, 9, 1));

            UserEducation existing = new UserEducation();
            existing.setId(educationId);
            existing.setUserId(userId);
            existing.setDegree("BSc");

            UserEducation saved = new UserEducation();
            saved.setDegree("MSc");

            UserEducationResponse response = new UserEducationResponse();
            response.setDegree("MSc");

            when(educationRepository.findById(educationId)).thenReturn(Optional.of(existing));
            when(educationRepository.save(existing)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserEducationResponse result = userService.updateEducation(userId, educationId, request);

            assertThat(result.getDegree()).isEqualTo("MSc");
            assertThat(existing.getDegree()).isEqualTo("MSc");
        }

        @Test
        @DisplayName("updateEducation - Không tìm thấy → ném EDUCATION_NOT_FOUND")
        void updateEducation_NotFound_ThrowsException() {
            UUID educationId = UUID.randomUUID();
            when(educationRepository.findById(educationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateEducation(userId, educationId, new UserEducationRequest()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getExceptionCode()).isEqualTo(UserErrorCode.EDUCATION_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("updateEducation - Tìm thấy nhưng thuộc user khác → ném EDUCATION_NOT_FOUND")
        void updateEducation_WrongUser_ThrowsException() {
            UUID educationId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();

            UserEducation education = new UserEducation();
            education.setId(educationId);
            education.setUserId(otherUserId); // thuộc user khác

            when(educationRepository.findById(educationId)).thenReturn(Optional.of(education));

            assertThatThrownBy(() -> userService.updateEducation(userId, educationId, new UserEducationRequest()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.EDUCATION_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("deleteEducation - Tìm thấy và thuộc user → xóa thành công")
        void deleteEducation_Found_Deletes() {
            UUID educationId = UUID.randomUUID();
            UserEducation education = new UserEducation();
            education.setId(educationId);
            education.setUserId(userId);

            when(educationRepository.findById(educationId)).thenReturn(Optional.of(education));

            userService.deleteEducation(userId, educationId);

            verify(educationRepository).delete(education);
        }

        @Test
        @DisplayName("deleteEducation - Không tìm thấy → ném EDUCATION_NOT_FOUND")
        void deleteEducation_NotFound_ThrowsException() {
            UUID educationId = UUID.randomUUID();
            when(educationRepository.findById(educationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteEducation(userId, educationId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.EDUCATION_NOT_FOUND);
                    });
        }
    }

    // =========================================================================
    // LANGUAGE
    // =========================================================================
    @Nested
    @DisplayName("UserService - Language")
    class LanguageServiceTests {

        @Test
        @DisplayName("getLanguages - Trả về danh sách ngôn ngữ đã map")
        void getLanguages_ReturnsMappedList() {
            UserLanguage lang = new UserLanguage();
            lang.setId(UUID.randomUUID());
            lang.setUserId(userId);
            lang.setLanguage("EN");

            UserLanguageResponse response = new UserLanguageResponse();
            response.setLanguage("EN");
            response.setLevel("ADVANCED");

            when(languageRepository.findByUserId(userId)).thenReturn(List.of(lang));
            when(userMapper.toResponse(lang)).thenReturn(response);

            List<UserLanguageResponse> result = userService.getLanguages(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLanguage()).isEqualTo("EN");
        }

        @Test
        @DisplayName("addLanguage - Lưu và trả về response")
        void addLanguage_SavesAndReturns() {
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("JP");
            request.setLevel("INTERMEDIATE");

            UserLanguage entity = new UserLanguage();
            UserLanguage saved = new UserLanguage();
            saved.setId(UUID.randomUUID());

            UserLanguageResponse response = new UserLanguageResponse();
            response.setLanguage("JP");

            when(userMapper.toEntity(request, userId)).thenReturn(entity);
            when(languageRepository.save(entity)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserLanguageResponse result = userService.addLanguage(userId, request);

            assertThat(result.getLanguage()).isEqualTo("JP");
        }

        @Test
        @DisplayName("updateLanguage - Không tìm thấy → ném LANGUAGE_NOT_FOUND")
        void updateLanguage_NotFound_ThrowsException() {
            UUID languageId = UUID.randomUUID();
            when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateLanguage(userId, languageId, new UserLanguageRequest()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.LANGUAGE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("updateLanguage - Thuộc user khác → ném LANGUAGE_NOT_FOUND")
        void updateLanguage_WrongUser_ThrowsException() {
            UUID languageId = UUID.randomUUID();
            UserLanguage language = new UserLanguage();
            language.setId(languageId);
            language.setUserId(UUID.randomUUID()); // user khác

            when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

            assertThatThrownBy(() -> userService.updateLanguage(userId, languageId, new UserLanguageRequest()))
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("updateLanguage - Tìm thấy → cập nhật language và level")
        void updateLanguage_Found_Updates() {
            UUID languageId = UUID.randomUUID();
            UserLanguageRequest request = new UserLanguageRequest();
            request.setLanguage("DE");
            request.setLevel("FLUENT");

            UserLanguage existing = new UserLanguage();
            existing.setId(languageId);
            existing.setUserId(userId);
            existing.setLanguage("FR");

            UserLanguage saved = new UserLanguage();
            saved.setLanguage("DE");
            saved.setLevel("FLUENT");

            UserLanguageResponse response = new UserLanguageResponse();
            response.setLanguage("DE");
            response.setLevel("FLUENT");

            when(languageRepository.findById(languageId)).thenReturn(Optional.of(existing));
            when(languageRepository.save(existing)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserLanguageResponse result = userService.updateLanguage(userId, languageId, request);

            assertThat(result.getLanguage()).isEqualTo("DE");
            assertThat(result.getLevel()).isEqualTo("FLUENT");
            assertThat(existing.getLanguage()).isEqualTo("DE");
            assertThat(existing.getLevel()).isEqualTo("FLUENT");
        }

        @Test
        @DisplayName("deleteLanguage - Tìm thấy → xóa thành công")
        void deleteLanguage_Found_Deletes() {
            UUID languageId = UUID.randomUUID();
            UserLanguage language = new UserLanguage();
            language.setId(languageId);
            language.setUserId(userId);

            when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));

            userService.deleteLanguage(userId, languageId);

            verify(languageRepository).delete(language);
        }

        @Test
        @DisplayName("deleteLanguage - Không tìm thấy → ném LANGUAGE_NOT_FOUND")
        void deleteLanguage_NotFound_ThrowsException() {
            UUID languageId = UUID.randomUUID();
            when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteLanguage(userId, languageId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.LANGUAGE_NOT_FOUND);
                    });
        }
    }

    // =========================================================================
    // EXPERIENCE
    // =========================================================================
    @Nested
    @DisplayName("UserService - Experience")
    class ExperienceServiceTests {

        @Test
        @DisplayName("getExperiences - Trả về danh sách kinh nghiệm")
        void getExperiences_ReturnsMappedList() {
            UserExperience exp = new UserExperience();
            exp.setId(UUID.randomUUID());
            exp.setUserId(userId);
            exp.setCompany("FPT");

            UserExperienceResponse response = new UserExperienceResponse();
            response.setCompany("FPT");

            when(experienceRepository.findByUserId(userId)).thenReturn(List.of(exp));
            when(userMapper.toResponse(exp)).thenReturn(response);

            List<UserExperienceResponse> result = userService.getExperiences(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCompany()).isEqualTo("FPT");
        }

        @Test
        @DisplayName("addExperience - Lưu và trả về response")
        void addExperience_SavesAndReturns() {
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("Viettel");
            request.setPosition("Backend Developer");
            request.setStartDate(LocalDate.of(2022, 1, 1));

            UserExperience entity = new UserExperience();
            UserExperience saved = new UserExperience();
            saved.setId(UUID.randomUUID());
            saved.setCompany("Viettel");

            UserExperienceResponse response = new UserExperienceResponse();
            response.setCompany("Viettel");

            when(userMapper.toEntity(request, userId)).thenReturn(entity);
            when(experienceRepository.save(entity)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserExperienceResponse result = userService.addExperience(userId, request);

            assertThat(result.getCompany()).isEqualTo("Viettel");
        }

        @Test
        @DisplayName("updateExperience - Thuộc user khác → ném EXPERIENCE_NOT_FOUND")
        void updateExperience_WrongUser_ThrowsException() {
            UUID expId = UUID.randomUUID();
            UserExperience exp = new UserExperience();
            exp.setId(expId);
            exp.setUserId(UUID.randomUUID()); // user khác

            when(experienceRepository.findById(expId)).thenReturn(Optional.of(exp));

            assertThatThrownBy(() -> userService.updateExperience(userId, expId, new UserExperienceRequest()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.EXPERIENCE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("updateExperience - Cập nhật isCurrent khi null không thay đổi giá trị cũ")
        void updateExperience_NullIsCurrent_KeepsOldValue() {
            UUID expId = UUID.randomUUID();
            UserExperienceRequest request = new UserExperienceRequest();
            request.setCompany("NewCorp");
            request.setPosition("CTO");
            request.setStartDate(LocalDate.of(2023, 1, 1));
            request.setIsCurrent(null); // không cập nhật isCurrent

            UserExperience existing = new UserExperience();
            existing.setId(expId);
            existing.setUserId(userId);
            existing.setIsCurrent(true); // giá trị cũ

            UserExperience saved = new UserExperience();
            UserExperienceResponse response = new UserExperienceResponse();

            when(experienceRepository.findById(expId)).thenReturn(Optional.of(existing));
            when(experienceRepository.save(existing)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            userService.updateExperience(userId, expId, request);

            // isCurrent giữ nguyên true vì request.getIsCurrent() == null
            assertThat(existing.getIsCurrent()).isTrue();
        }

        @Test
        @DisplayName("deleteExperience - Không tìm thấy → ném EXPERIENCE_NOT_FOUND")
        void deleteExperience_NotFound_ThrowsException() {
            UUID expId = UUID.randomUUID();
            when(experienceRepository.findById(expId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteExperience(userId, expId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.EXPERIENCE_NOT_FOUND);
                    });
        }
    }

    // =========================================================================
    // CERTIFICATE
    // =========================================================================
    @Nested
    @DisplayName("UserService - Certificate")
    class CertificateServiceTests {

        @Test
        @DisplayName("getCertificates - Trả về danh sách chứng chỉ")
        void getCertificates_ReturnsMappedList() {
            UserCertificate cert = new UserCertificate();
            cert.setId(UUID.randomUUID());
            cert.setUserId(userId);
            cert.setName("AWS Certified");

            UserCertificateResponse response = new UserCertificateResponse();
            response.setName("AWS Certified");

            when(certificateRepository.findByUserId(userId)).thenReturn(List.of(cert));
            when(userMapper.toResponse(cert)).thenReturn(response);

            List<UserCertificateResponse> result = userService.getCertificates(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("AWS Certified");
        }

        @Test
        @DisplayName("addCertificate - Lưu và trả về response")
        void addCertificate_SavesAndReturns() {
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("GCP Certified");
            request.setOrganization("Google");
            request.setIssueDate(LocalDate.of(2024, 3, 1));

            UserCertificate entity = new UserCertificate();
            UserCertificate saved = new UserCertificate();
            saved.setId(UUID.randomUUID());

            UserCertificateResponse response = new UserCertificateResponse();
            response.setName("GCP Certified");

            when(userMapper.toEntity(request, userId)).thenReturn(entity);
            when(certificateRepository.save(entity)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserCertificateResponse result = userService.addCertificate(userId, request);

            assertThat(result.getName()).isEqualTo("GCP Certified");
        }

        @Test
        @DisplayName("updateCertificate - Tìm thấy → cập nhật tất cả trường")
        void updateCertificate_Found_UpdatesAllFields() {
            UUID certId = UUID.randomUUID();
            UserCertificateRequest request = new UserCertificateRequest();
            request.setName("Azure Certified");
            request.setOrganization("Microsoft");
            request.setIssueDate(LocalDate.of(2024, 6, 1));
            request.setExpirationDate(LocalDate.of(2026, 6, 1));
            request.setDescription("Cloud certification");

            UserCertificate existing = new UserCertificate();
            existing.setId(certId);
            existing.setUserId(userId);
            existing.setName("Old Name");

            UserCertificate saved = new UserCertificate();
            UserCertificateResponse response = new UserCertificateResponse();
            response.setName("Azure Certified");

            when(certificateRepository.findById(certId)).thenReturn(Optional.of(existing));
            when(certificateRepository.save(existing)).thenReturn(saved);
            when(userMapper.toResponse(saved)).thenReturn(response);

            UserCertificateResponse result = userService.updateCertificate(userId, certId, request);

            assertThat(result.getName()).isEqualTo("Azure Certified");
            // Xác minh các trường đã được cập nhật trên entity
            assertThat(existing.getName()).isEqualTo("Azure Certified");
            assertThat(existing.getOrganization()).isEqualTo("Microsoft");
            assertThat(existing.getDescription()).isEqualTo("Cloud certification");
        }

        @Test
        @DisplayName("updateCertificate - Thuộc user khác → ném CERTIFICATE_NOT_FOUND")
        void updateCertificate_WrongUser_ThrowsException() {
            UUID certId = UUID.randomUUID();
            UserCertificate cert = new UserCertificate();
            cert.setId(certId);
            cert.setUserId(UUID.randomUUID()); // user khác

            when(certificateRepository.findById(certId)).thenReturn(Optional.of(cert));

            assertThatThrownBy(() -> userService.updateCertificate(userId, certId, new UserCertificateRequest()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.CERTIFICATE_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("deleteCertificate - Tìm thấy → xóa thành công")
        void deleteCertificate_Found_Deletes() {
            UUID certId = UUID.randomUUID();
            UserCertificate cert = new UserCertificate();
            cert.setId(certId);
            cert.setUserId(userId);

            when(certificateRepository.findById(certId)).thenReturn(Optional.of(cert));

            userService.deleteCertificate(userId, certId);

            verify(certificateRepository).delete(cert);
        }

        @Test
        @DisplayName("deleteCertificate - Không tìm thấy → ném CERTIFICATE_NOT_FOUND")
        void deleteCertificate_NotFound_ThrowsException() {
            UUID certId = UUID.randomUUID();
            when(certificateRepository.findById(certId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteCertificate(userId, certId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        assertThat(((AppException) ex).getExceptionCode()).isEqualTo(UserErrorCode.CERTIFICATE_NOT_FOUND);
                    });
        }
    }
}
