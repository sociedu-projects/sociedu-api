package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho UserProfile trong context module profile.
 * Sử dụng entity từ module user, cung cấp query methods riêng cho profile module.
 */
@Repository
public interface ProfileUserProfileRepository extends JpaRepository<UserProfile, Long> {
}
