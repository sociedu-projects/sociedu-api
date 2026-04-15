package com.unishare.api.modules.trust.service;

import com.unishare.api.modules.trust.dto.*;

import java.util.List;

public interface TrustService {

    ModerationReportResponse createReport(Long reporterId, CreateModerationReportRequest request);

    List<ModerationReportResponse> myReports(Long reporterId);

    ModerationReportResponse addEvidence(Long reporterId, Long reportId, AddReportEvidenceRequest request);

    ModerationReportResponse resolveReport(Long moderatorUserId, Long reportId, ResolveReportRequest request);

    DisputeResponse createDispute(Long userId, CreateDisputeRequest request);

    List<DisputeResponse> myDisputes(Long userId);

    DisputeResponse resolveDispute(Long moderatorUserId, Long disputeId, ResolveDisputeRequest request);
}
