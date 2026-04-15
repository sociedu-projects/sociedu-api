package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {
    List<MentorProfile> findByVerificationStatus(String status);
}
