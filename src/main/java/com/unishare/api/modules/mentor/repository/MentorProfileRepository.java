package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long>,
        JpaSpecificationExecutor<MentorProfile> {

    List<MentorProfile> findByVerificationStatus(String status);

    Page<MentorProfile> findByVerificationStatus(String status, Pageable pageable);
}
