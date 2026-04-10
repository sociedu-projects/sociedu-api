package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.user.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho UserEducation trong context module profile.
 */
@Repository
public interface ProfileEducationRepository extends JpaRepository<UserEducation, Long> {
    List<UserEducation> findByUserId(Long userId);
}
