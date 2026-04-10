package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.profile.entity.MentorPayoutInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilePayoutInfoRepository extends JpaRepository<MentorPayoutInfo, Long> {
    Optional<MentorPayoutInfo> findByMentorId(Long mentorId);
}
