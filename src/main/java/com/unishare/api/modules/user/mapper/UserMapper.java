package com.unishare.api.modules.user.mapper;

import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(UserProfile entity) {
        if (entity == null) return null;
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(entity.getUserId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setHeadline(entity.getHeadline());
        response.setAvatarFileId(entity.getAvatarFileId());
        response.setBio(entity.getBio());
        response.setLocation(entity.getLocation());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public void updateEntity(UserProfile entity, UserProfileRequest request) {
        if (request.getFirstName() != null) entity.setFirstName(request.getFirstName());
        if (request.getLastName() != null) entity.setLastName(request.getLastName());
        if (request.getHeadline() != null) entity.setHeadline(request.getHeadline());
        if (request.getAvatarFileId() != null) entity.setAvatarFileId(request.getAvatarFileId());
        if (request.getBio() != null) entity.setBio(request.getBio());
        if (request.getLocation() != null) entity.setLocation(request.getLocation());
    }

    public UserEducationResponse toResponse(UserEducation entity) {
        if (entity == null) return null;
        UserEducationResponse response = new UserEducationResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setUniversityId(entity.getUniversityId());
        response.setMajorId(entity.getMajorId());
        if (entity.getUniversity() != null) {
            response.setUniversityName(entity.getUniversity().getName());
        }
        if (entity.getMajorField() != null) {
            response.setMajorName(entity.getMajorField().getName());
        }
        response.setDegree(entity.getDegree());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setIsCurrent(entity.getIsCurrent());
        response.setDescription(entity.getDescription());
        return response;
    }

    public UserEducation toEntity(UserEducationRequest request, UUID userId) {
        if (request == null) return null;
        UserEducation entity = new UserEducation();
        entity.setUserId(userId);
        entity.setUniversityId(request.getUniversityId());
        entity.setMajorId(request.getMajorId());
        entity.setDegree(request.getDegree());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
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

    public UserLanguage toEntity(UserLanguageRequest request, UUID userId) {
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
        response.setIsCurrent(entity.getIsCurrent());
        response.setDescription(entity.getDescription());
        return response;
    }

    public UserExperience toEntity(UserExperienceRequest request, UUID userId) {
        if (request == null) return null;
        UserExperience entity = new UserExperience();
        entity.setUserId(userId);
        entity.setCompany(request.getCompany());
        entity.setPosition(request.getPosition());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false);
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
        response.setCredentialFileId(entity.getCredentialFileId());
        response.setDescription(entity.getDescription());
        return response;
    }

    public UserCertificate toEntity(UserCertificateRequest request, UUID userId) {
        if (request == null) return null;
        UserCertificate entity = new UserCertificate();
        entity.setUserId(userId);
        entity.setName(request.getName());
        entity.setOrganization(request.getOrganization());
        entity.setIssueDate(request.getIssueDate());
        entity.setExpirationDate(request.getExpirationDate());
        entity.setCredentialFileId(request.getCredentialFileId());
        entity.setDescription(request.getDescription());
        return entity;
    }
}
