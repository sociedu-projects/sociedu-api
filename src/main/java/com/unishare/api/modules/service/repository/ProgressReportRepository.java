package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.ProgressReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressReportRepository extends JpaRepository<ProgressReport, UUID> {
    List<ProgressReport> findByMenteeIdOrderByCreatedAtDesc(UUID menteeId);
    Page<ProgressReport> findByMenteeId(UUID menteeId, Pageable pageable);

    List<ProgressReport> findByMentorIdOrderByCreatedAtDesc(UUID mentorId);

    Page<ProgressReport> findByMentorId(UUID mentorId, Pageable pageable);
}
