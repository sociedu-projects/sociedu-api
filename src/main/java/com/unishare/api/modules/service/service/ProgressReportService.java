package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.request.CreateReportRequest;
import com.unishare.api.modules.service.dto.request.ReviewReportRequest;
import com.unishare.api.modules.service.dto.response.ProgressReportResponse;

import java.util.List;
import java.util.UUID;

public interface ProgressReportService {
    // For Mentees
    ProgressReportResponse createReport(UUID menteeId, CreateReportRequest request);
    List<ProgressReportResponse> getMenteeReports(UUID menteeId);

    // For Mentors
    List<ProgressReportResponse> getMentorReports(UUID mentorId);
    ProgressReportResponse reviewReport(UUID mentorId, UUID reportId, ReviewReportRequest request);
}
