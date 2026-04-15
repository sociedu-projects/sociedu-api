package com.unishare.api.modules.trust.repository;

import com.unishare.api.modules.trust.entity.ModerationReportEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModerationReportEvidenceRepository extends JpaRepository<ModerationReportEvidence, UUID> {

    List<ModerationReportEvidence> findByReportId(UUID reportId);
}
