package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.service.CatalogService;
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

    private MentorCatalogController mentorController;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = Mockito.mock(CatalogService.class);
        mentorController = new MentorCatalogController(catalogService);
    }

    @Test
    void listCurriculum_whenPageParamsProvided_shouldReturnPagedData() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        // Use page=0 to avoid Spring's total adjustment (offset=0, no adjustment needed)
        PageRequest pageable = PageRequest.of(0, 5);
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

        when(catalogService.listCurriculum(eq(mentorId), eq(packageId), eq(versionId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        ResponseEntity<ApiResponse<Page<MentorDto.CurriculumItemResponse>>> result =
                mentorController.listCurriculum(principal, packageId, versionId, pageable);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getData().getTotalElements());
        assertEquals("Session 1", result.getBody().getData().getContent().get(0).getTitle());
    }
}
