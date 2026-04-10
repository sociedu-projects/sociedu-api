package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.profile.entity.MentorReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileReviewHistoryRepository extends JpaRepository<MentorReviewHistory, Long> {
    List<MentorReviewHistory> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
}
