package com.unishare.api.modules.trust.service;

import com.unishare.api.modules.trust.dto.*;

import java.util.List;
import java.util.UUID;

public interface TrustService {

    ModerationReportResponse createReport(UUID reporterId, CreateModerationReportRequest request);

    List<ModerationReportResponse> myReports(UUID reporterId);

    ModerationReportResponse addEvidence(UUID reporterId, UUID reportId, AddReportEvidenceRequest request);

    ModerationReportResponse resolveReport(UUID moderatorUserId, UUID reportId, ResolveReportRequest request);

    DisputeResponse createDispute(UUID userId, CreateDisputeRequest request);

    List<DisputeResponse> myDisputes(UUID userId);

    DisputeResponse resolveDispute(UUID moderatorUserId, UUID disputeId, ResolveDisputeRequest request);
}
