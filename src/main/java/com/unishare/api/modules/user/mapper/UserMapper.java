package com.unishare.api.modules.user.mapper;

import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.*;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(UserProfile entity) {
        if (entity == null) return null;
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setAvatarUrl(entity.getAvatarUrl());
        response.setBio(entity.getBio());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public void updateEntity(UserProfile entity, UserProfileRequest request) {
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) entity.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) entity.setBio(request.getBio());
    }

    public UserEducationResponse toResponse(UserEducation entity) {
        if (entity == null) return null;
        UserEducationResponse response = new UserEducationResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setUniversity(entity.getUniversity());
        response.setMajor(entity.getMajor());
        response.setStartYear(entity.getStartYear());
        response.setEndYear(entity.getEndYear());
        response.setDescription(entity.getDescription());
        return response;
    }

    public UserEducation toEntity(UserEducationRequest request, Long userId) {
        if (request == null) return null;
        UserEducation entity = new UserEducation();
        entity.setUserId(userId);
        entity.setUniversity(request.getUniversity());
        entity.setMajor(request.getMajor());
        entity.setStartYear(request.getStartYear());
        entity.setEndYear(request.getEndYear());
        entity.setDescription(request.getDescription());
        return entity;
    }

    public UserLanguageResponse toResponse(UserLanguage entity) {
        if (entity == null) return null;
        UserLanguageResponse response = new UserLanguageResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setLanguage(entity.getLanguage());
        response.setLevel(entity.getLevel());
        return response;
    }

    public UserLanguage toEntity(UserLanguageRequest request, Long userId) {
        if (request == null) return null;
        UserLanguage entity = new UserLanguage();
        entity.setUserId(userId);
        entity.setLanguage(request.getLanguage());
        entity.setLevel(request.getLevel());
        return entity;
    }

    public UserExperienceResponse toResponse(UserExperience entity) {
        if (entity == null) return null;
        UserExperienceResponse response = new UserExperienceResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setCompany(entity.getCompany());
        response.setPosition(entity.getPosition());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setDescription(entity.getDescription());
        return response;
    }

    public UserExperience toEntity(UserExperienceRequest request, Long userId) {
        if (request == null) return null;
        UserExperience entity = new UserExperience();
        entity.setUserId(userId);
        entity.setCompany(request.getCompany());
        entity.setPosition(request.getPosition());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setDescription(request.getDescription());
        return entity;
    }

    public UserCertificateResponse toResponse(UserCertificate entity) {
        if (entity == null) return null;
        UserCertificateResponse response = new UserCertificateResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setName(entity.getName());
        response.setOrganization(entity.getOrganization());
        response.setIssueDate(entity.getIssueDate());
        response.setExpirationDate(entity.getExpirationDate());
        response.setCredentialUrl(entity.getCredentialUrl());
        return response;
    }

    public UserCertificate toEntity(UserCertificateRequest request, Long userId) {
        if (request == null) return null;
        UserCertificate entity = new UserCertificate();
        entity.setUserId(userId);
        entity.setName(request.getName());
        entity.setOrganization(request.getOrganization());
        entity.setIssueDate(request.getIssueDate());
        entity.setExpirationDate(request.getExpirationDate());
        entity.setCredentialUrl(request.getCredentialUrl());
        return entity;
    }
}
