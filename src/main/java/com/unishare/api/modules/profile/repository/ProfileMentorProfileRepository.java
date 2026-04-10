package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho MentorProfile trong context module profile (onboarding).
 * Sử dụng entity từ module mentor.
 */
@Repository
public interface ProfileMentorProfileRepository extends JpaRepository<MentorProfile, Long> {
}
