package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.request.CreateReportRequest;
import com.unishare.api.modules.service.dto.request.ReviewReportRequest;
import com.unishare.api.modules.service.dto.response.ProgressReportResponse;

import java.util.List;

public interface ProgressReportService {
    // For Mentees
    ProgressReportResponse createReport(Long menteeId, CreateReportRequest request);
    List<ProgressReportResponse> getMenteeReports(Long menteeId);

    // For Mentors
    List<ProgressReportResponse> getMentorReports(Long mentorId);
    ProgressReportResponse reviewReport(Long mentorId, Long reportId, ReviewReportRequest request);
}
