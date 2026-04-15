package com.unishare.api.modules.trust.repository;

import com.unishare.api.modules.trust.entity.ModerationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, UUID> {

    List<ModerationReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
}
