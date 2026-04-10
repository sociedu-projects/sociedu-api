package com.unishare.api.modules.profile.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.infrastructure.storage.FileStorageService;
import com.unishare.api.modules.profile.dto.ProfileDto.AvatarUploadResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.MyProfileResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.UpdateProfileRequest;
import com.unishare.api.modules.profile.exception.ProfileErrorCode;
import com.unishare.api.modules.profile.mapper.ProfileMapper;
import com.unishare.api.modules.profile.repository.*;
import com.unishare.api.modules.profile.service.ProfileService;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.entity.UserProfile;
import com.unishare.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileUserProfileRepository profileRepository;
    private final ProfileEducationRepository educationRepository;
    private final ProfileLanguageRepository languageRepository;
    private final ProfileExperienceRepository experienceRepository;
    private final ProfileCertificateRepository certificateRepository;
    private final FileStorageService fileStorageService;
    private final ProfileMapper profileMapper;
    private final UserMapper userMapper;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final String AVATAR_FOLDER = "avatars";

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        UserProfile profile = findProfileOrThrow(userId);

        List<UserEducationResponse> educations = educationRepository.findByUserId(userId)
                .stream().map(userMapper::toResponse).collect(Collectors.toList());
        List<UserLanguageResponse> languages = languageRepository.findByUserId(userId)
                .stream().map(userMapper::toResponse).collect(Collectors.toList());
        List<UserExperienceResponse> experiences = experienceRepository.findByUserId(userId)
                .stream().map(userMapper::toResponse).collect(Collectors.toList());
        List<UserCertificateResponse> certificates = certificateRepository.findByUserId(userId)
                .stream().map(userMapper::toResponse).collect(Collectors.toList());

        return profileMapper.toMyProfileResponse(profile, educations, languages, experiences, certificates);
    }

    @Override
    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElse(createNewProfile(userId));

        applyPartialUpdate(profile, request);
        profileRepository.save(profile);

        log.info("User {} updated profile", userId);
        return getMyProfile(userId);
    }

    @Override
    @Transactional
    public AvatarUploadResponse uploadAvatar(Long userId, MultipartFile file) {
        validateAvatarFile(file);

        UserProfile profile = profileRepository.findById(userId)
                .orElse(createNewProfile(userId));

        String avatarUrl = fileStorageService.uploadFile(file, AVATAR_FOLDER);
        profile.setAvatarUrl(avatarUrl);
        profileRepository.save(profile);

        log.info("User {} uploaded new avatar", userId);
        return AvatarUploadResponse.builder().avatarUrl(avatarUrl).build();
    }

    // ======================== PRIVATE HELPERS ========================

    private UserProfile findProfileOrThrow(Long userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new AppException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

    private UserProfile createNewProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        return profile;
    }

    /**
     * Partial update: chỉ set field khi giá trị request không null.
     */
    private void applyPartialUpdate(UserProfile profile, UpdateProfileRequest request) {
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ProfileErrorCode.INVALID_FILE_TYPE, "File is empty");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new AppException(ProfileErrorCode.INVALID_FILE_TYPE,
                    "Only JPEG, PNG and WebP images are allowed");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new AppException(ProfileErrorCode.FILE_TOO_LARGE,
                    "Avatar file must not exceed 5MB");
        }
    }
}
