package com.unishare.api.modules.user.service;

import com.unishare.api.modules.user.dto.*;

import java.util.List;

public interface UserService {

    // Profile
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UserProfileRequest request);

    // Education
    List<UserEducationResponse> getEducations(Long userId);
    UserEducationResponse addEducation(Long userId, UserEducationRequest request);
    UserEducationResponse updateEducation(Long userId, Long educationId, UserEducationRequest request);
    void deleteEducation(Long userId, Long educationId);

    // Language
    List<UserLanguageResponse> getLanguages(Long userId);
    UserLanguageResponse addLanguage(Long userId, UserLanguageRequest request);
    UserLanguageResponse updateLanguage(Long userId, Long languageId, UserLanguageRequest request);
    void deleteLanguage(Long userId, Long languageId);

    // Experience
    List<UserExperienceResponse> getExperiences(Long userId);
    UserExperienceResponse addExperience(Long userId, UserExperienceRequest request);
    UserExperienceResponse updateExperience(Long userId, Long experienceId, UserExperienceRequest request);
    void deleteExperience(Long userId, Long experienceId);

    // Certificate
    List<UserCertificateResponse> getCertificates(Long userId);
    UserCertificateResponse addCertificate(Long userId, UserCertificateRequest request);
    UserCertificateResponse updateCertificate(Long userId, Long certificateId, UserCertificateRequest request);
    void deleteCertificate(Long userId, Long certificateId);
}

