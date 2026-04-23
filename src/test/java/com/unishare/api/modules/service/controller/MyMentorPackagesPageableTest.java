package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MyMentorPackagesPageableTest {

    private MentorController mentorController;
    private MentorService mentorService;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        mentorController = new MentorController(mentorService);
    }

    @Test
    void getMyPackages_whenPageParamsProvided_shouldReturnPagedData() {
        UUID mentorId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(1, 5);
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );

        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(UUID.randomUUID())
                .mentorId(mentorId)
                .name("Career Planning")
                .isActive(false)
                .build();

        when(mentorService.getMyPackages(eq(mentorId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        ResponseEntity<ApiResponse<Page<MentorDto.ServicePackageResponse>>> result =
                mentorController.getMyPackages(principal, pageable);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getData().getTotalElements());
        assertEquals("Career Planning", result.getBody().getData().getContent().get(0).getName());
        assertEquals(false, result.getBody().getData().getContent().get(0).getIsActive());
    }

    @Test
    void getMyPackage_whenPackageExists_shouldReturnPackageDetail() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );

        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(packageId)
                .mentorId(mentorId)
                .name("Career Planning")
                .isActive(false)
                .build();

        when(mentorService.getMyPackage(eq(mentorId), eq(packageId))).thenReturn(response);

        ResponseEntity<ApiResponse<MentorDto.ServicePackageResponse>> result =
                mentorController.getMyPackage(principal, packageId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(packageId, result.getBody().getData().getId());
        assertEquals("Career Planning", result.getBody().getData().getName());
    }
}
