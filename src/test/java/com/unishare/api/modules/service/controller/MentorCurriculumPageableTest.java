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

class MentorCurriculumPageableTest {

    private MentorController mentorController;
    private MentorService mentorService;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        mentorController = new MentorController(mentorService);
    }

    @Test
    void listCurriculum_whenPageParamsProvided_shouldReturnPagedData() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(1, 5);
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );

        MentorDto.CurriculumItemResponse response = MentorDto.CurriculumItemResponse.builder()
                .id(UUID.randomUUID())
                .packageVersionId(versionId)
                .title("Session 1")
                .orderIndex(1)
                .duration(60)
                .build();

        when(mentorService.listCurriculum(eq(mentorId), eq(packageId), eq(versionId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        ResponseEntity<ApiResponse<Page<MentorDto.CurriculumItemResponse>>> result =
                mentorController.listCurriculum(principal, packageId, versionId, pageable);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getData().getTotalElements());
        assertEquals("Session 1", result.getBody().getData().getContent().get(0).getTitle());
    }
}
