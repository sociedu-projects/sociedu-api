package com.unishare.api.modules.admin.service;

import com.unishare.api.modules.admin.dto.AdminDto.AdminStatsResponse;
import com.unishare.api.modules.document.dto.DocumentResponse;
import com.unishare.api.modules.mentor.dto.MentorDto.MentorProfileResponse;

import java.util.List;

public interface AdminService {
    AdminStatsResponse getSystemStats();

    // Mentor
    List<MentorProfileResponse> getPendingMentorRequests();
    MentorProfileResponse approveMentorRequest(Long mentorId);

    // Document
    List<DocumentResponse> getPendingProductRequests();
    DocumentResponse approveProductRequest(Long documentId);
}
