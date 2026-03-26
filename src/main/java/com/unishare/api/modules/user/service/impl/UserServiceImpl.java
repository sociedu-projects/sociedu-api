package com.unishare.api.modules.user.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.*;
import com.unishare.api.modules.user.exception.UserErrorCode;
import com.unishare.api.modules.user.mapper.UserMapper;
import com.unishare.api.modules.user.repository.*;
import com.unishare.api.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository profileRepository;
    private final UserEducationRepository educationRepository;
    private final UserLanguageRepository languageRepository;
    private final UserExperienceRepository experienceRepository;
    private final UserCertificateRepository certificateRepository;
    private final UserMapper userMapper;

    // Profile
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(UserErrorCode.PROFILE_NOT_FOUND));
        return userMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElse(new UserProfile()); // Support creating profile on first update
        
        if (profile.getUserId() == null) {
            profile.setUserId(userId);
        }
        
        userMapper.updateEntity(profile, request);
        UserProfile savedProfile = profileRepository.save(profile);
        return userMapper.toResponse(savedProfile);
    }

    // Education
    @Override
    @Transactional(readOnly = true)
    public List<UserEducationResponse> getEducations(Long userId) {
        return educationRepository.findByUserId(userId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserEducationResponse addEducation(Long userId, UserEducationRequest request) {
        UserEducation education = userMapper.toEntity(request, userId);
        UserEducation saved = educationRepository.save(education);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserEducationResponse updateEducation(Long userId, Long educationId, UserEducationRequest request) {
        UserEducation education = educationRepository.findById(educationId)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.EDUCATION_NOT_FOUND));
        
        education.setUniversity(request.getUniversity());
        education.setMajor(request.getMajor());
        education.setStartYear(request.getStartYear());
        education.setEndYear(request.getEndYear());
        education.setDescription(request.getDescription());
        
        UserEducation saved = educationRepository.save(education);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEducation(Long userId, Long educationId) {
        UserEducation education = educationRepository.findById(educationId)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.EDUCATION_NOT_FOUND));
        educationRepository.delete(education);
    }

    // Language
    @Override
    @Transactional(readOnly = true)
    public List<UserLanguageResponse> getLanguages(Long userId) {
        return languageRepository.findByUserId(userId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserLanguageResponse addLanguage(Long userId, UserLanguageRequest request) {
        UserLanguage language = userMapper.toEntity(request, userId);
        UserLanguage saved = languageRepository.save(language);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserLanguageResponse updateLanguage(Long userId, Long languageId, UserLanguageRequest request) {
        UserLanguage language = languageRepository.findById(languageId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.LANGUAGE_NOT_FOUND));
        
        language.setLanguage(request.getLanguage());
        language.setLevel(request.getLevel());
        
        UserLanguage saved = languageRepository.save(language);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLanguage(Long userId, Long languageId) {
        UserLanguage language = languageRepository.findById(languageId)
                .filter(l -> l.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.LANGUAGE_NOT_FOUND));
        languageRepository.delete(language);
    }

    // Experience
    @Override
    @Transactional(readOnly = true)
    public List<UserExperienceResponse> getExperiences(Long userId) {
        return experienceRepository.findByUserId(userId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserExperienceResponse addExperience(Long userId, UserExperienceRequest request) {
        UserExperience experience = userMapper.toEntity(request, userId);
        UserExperience saved = experienceRepository.save(experience);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserExperienceResponse updateExperience(Long userId, Long experienceId, UserExperienceRequest request) {
        UserExperience experience = experienceRepository.findById(experienceId)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.EXPERIENCE_NOT_FOUND));
        
        experience.setCompany(request.getCompany());
        experience.setPosition(request.getPosition());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription());
        
        UserExperience saved = experienceRepository.save(experience);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        UserExperience experience = experienceRepository.findById(experienceId)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.EXPERIENCE_NOT_FOUND));
        experienceRepository.delete(experience);
    }

    // Certificate
    @Override
    @Transactional(readOnly = true)
    public List<UserCertificateResponse> getCertificates(Long userId) {
        return certificateRepository.findByUserId(userId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserCertificateResponse addCertificate(Long userId, UserCertificateRequest request) {
        UserCertificate certificate = userMapper.toEntity(request, userId);
        UserCertificate saved = certificateRepository.save(certificate);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserCertificateResponse updateCertificate(Long userId, Long certificateId, UserCertificateRequest request) {
        UserCertificate certificate = certificateRepository.findById(certificateId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.CERTIFICATE_NOT_FOUND));
        
        certificate.setName(request.getName());
        certificate.setOrganization(request.getOrganization());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setExpirationDate(request.getExpirationDate());
        certificate.setCredentialUrl(request.getCredentialUrl());
        
        UserCertificate saved = certificateRepository.save(certificate);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCertificate(Long userId, Long certificateId) {
        UserCertificate certificate = certificateRepository.findById(certificateId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(UserErrorCode.CERTIFICATE_NOT_FOUND));
        certificateRepository.delete(certificate);
    }
}

