package com.unishare.api.modules.service.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.service.dto.request.CreateReportRequest;
import com.unishare.api.modules.service.dto.request.ReviewReportRequest;
import com.unishare.api.modules.service.dto.response.ProgressReportResponse;
import com.unishare.api.modules.service.entity.ProgressReport;
import com.unishare.api.modules.service.exception.ProgressReportErrorCode;
import com.unishare.api.modules.service.repository.ProgressReportRepository;
import com.unishare.api.modules.service.service.ProgressReportService;
import com.unishare.api.modules.user.entity.UserProfile;
import com.unishare.api.modules.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressReportServiceImpl implements ProgressReportService {

    private final ProgressReportRepository progressReportRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public ProgressReportResponse createReport(Long menteeId, CreateReportRequest request) {
        ProgressReport report = new ProgressReport();
        report.setMenteeId(menteeId);
        report.setMentorId(request.getMentorId());
        report.setTitle(request.getTitle());
        report.setContent(request.getContent());
        report.setAttachmentUrl(request.getAttachmentUrl());

        ProgressReport saved = progressReportRepository.save(report);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressReportResponse> getMenteeReports(Long menteeId) {
        return progressReportRepository.findByMenteeIdOrderByCreatedAtDesc(menteeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressReportResponse> getMentorReports(Long mentorId) {
        return progressReportRepository.findByMentorIdOrderByCreatedAtDesc(mentorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProgressReportResponse reviewReport(Long mentorId, Long reportId, ReviewReportRequest request) {
        ProgressReport report = progressReportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ProgressReportErrorCode.PROGRESS_REPORT_NOT_FOUND,
                        "Không tìm thấy báo cáo này"));

        if (!report.getMentorId().equals(mentorId)) {
            throw new AppException(ProgressReportErrorCode.PROGRESS_REPORT_ACCESS_DENIED,
                    "Bạn không có quyền chấm báo cáo này");
        }

        report.setStatus(request.getStatus());
        report.setMentorFeedback(request.getMentorFeedback());
        ProgressReport saved = progressReportRepository.save(report);
        
        return mapToResponse(saved);
    }

    private ProgressReportResponse mapToResponse(ProgressReport report) {
        String menteeName = userProfileRepository.findById(report.getMenteeId())
                .map(UserProfile::getDisplayName)
                .orElse("Mentee");

        String mentorName = userProfileRepository.findById(report.getMentorId())
                .map(UserProfile::getDisplayName)
                .orElse("Mentor");

        return ProgressReportResponse.builder()
                .id(report.getId())
                .menteeId(report.getMenteeId())
                .mentorId(report.getMentorId())
                .menteeName(menteeName)
                .mentorName(mentorName)
                .title(report.getTitle())
                .content(report.getContent())
                .attachmentUrl(report.getAttachmentUrl())
                .status(report.getStatus())
                .mentorFeedback(report.getMentorFeedback())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
