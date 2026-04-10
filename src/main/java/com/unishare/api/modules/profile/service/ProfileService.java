package com.unishare.api.modules.profile.service;

import com.unishare.api.modules.profile.dto.ProfileDto.AvatarUploadResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.MyProfileResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    /**
     * Lấy hồ sơ đầy đủ của user (profile + educations + languages + experiences + certificates).
     */
    MyProfileResponse getMyProfile(Long userId);

    /**
     * Cập nhật thông tin cơ bản (fullName, bio). Partial update — chỉ set field non-null.
     */
    MyProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request);

    /**
     * Upload avatar: validate file type, upload lên storage, cập nhật avatar_url.
     */
    AvatarUploadResponse uploadAvatar(Long userId, MultipartFile file);
}
