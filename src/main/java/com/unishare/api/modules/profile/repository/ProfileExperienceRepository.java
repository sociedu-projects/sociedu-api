package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.user.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho UserExperience trong context module profile.
 */
@Repository
public interface ProfileExperienceRepository extends JpaRepository<UserExperience, Long> {
    List<UserExperience> findByUserId(Long userId);
}
