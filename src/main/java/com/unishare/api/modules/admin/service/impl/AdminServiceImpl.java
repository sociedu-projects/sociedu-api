package com.unishare.api.modules.admin.service.impl;

import com.unishare.api.modules.admin.dto.AdminDto.AdminStatsResponse;
import com.unishare.api.modules.admin.service.AdminService;
import com.unishare.api.modules.mentor.dto.MentorDto.MentorProfileResponse;
import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.mentor.repository.MentorProfileRepository;
import com.unishare.api.modules.mentor.service.MentorService;
import com.unishare.api.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MentorProfileRepository mentorProfileRepository;
    private final MentorService mentorService;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        long orderCount = orderRepository.count();

        long pendingMentor = mentorProfileRepository.findByVerificationStatus("pending").size();

        // Optional: Aggregate total sales
        BigDecimal totalSales = BigDecimal.ZERO;

        return AdminStatsResponse.builder()
                .totalSales(totalSales)
                .orderCount(orderCount)
                .recentOrders(Collections.emptyList()) // simplified
                .pendingMentorRequests(pendingMentor)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorProfileResponse> getPendingMentorRequests() {
        return mentorProfileRepository.findByVerificationStatus("pending").stream()
                .map(profile -> mentorService.getMentorProfile(profile.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MentorProfileResponse approveMentorRequest(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        profile.setVerificationStatus("verified");
        mentorProfileRepository.save(profile);
        return mentorService.getMentorProfile(mentorId);
    }

}
