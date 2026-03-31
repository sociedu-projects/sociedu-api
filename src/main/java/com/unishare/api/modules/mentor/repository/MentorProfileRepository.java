package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    List<MentorProfile> findByVerificationStatus(String status);
}
