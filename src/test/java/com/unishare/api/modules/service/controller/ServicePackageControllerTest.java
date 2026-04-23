package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemRequest;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemResponse;
import com.unishare.api.modules.service.dto.request.CreateServicePackageVersionRequest;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.dto.request.UpdateServicePackageRequest;
import com.unishare.api.modules.service.service.MentorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServicePackageControllerTest {

    private MockMvc mockMvc;
    private MentorService mentorService;

    @BeforeEach
    void setUp() {
        mentorService = Mockito.mock(MentorService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ServicePackageController(mentorService)).build();
    }

    @Test
    void getActivePackage_shouldReturnPackageDetail() throws Exception {
        UUID packageId = UUID.randomUUID();
        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(packageId)
                .mentorId(UUID.randomUUID())
                .name("Career Planning")
                .description("Package description")
                .isActive(true)
                .build();

        when(mentorService.getActivePackage(eq(packageId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/service-packages/{id}", packageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(packageId.toString()))
                .andExpect(jsonPath("$.data.name").value("Career Planning"))
                .andExpect(jsonPath("$.data.description").value("Package description"));
    }

    @Test
    void createPackage_shouldDelegateToServiceAndReturnCreatedPackage() {
        UUID mentorId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        CreateServicePackageRequest request = new CreateServicePackageRequest();
        request.setName("Career Planning");
        request.setDescription("Package description");
        request.setPrice(new java.math.BigDecimal("120.00"));
        request.setDuration(3);
        request.setDeliveryType("ONLINE");
        CreateServicePackageRequest.CurriculumRequest curriculum = new CreateServicePackageRequest.CurriculumRequest();
        curriculum.setTitle("Session 1");
        curriculum.setOrderIndex(1);
        curriculum.setDuration(60);
        request.setCurriculums(List.of(curriculum));

        UUID packageId = UUID.randomUUID();
        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(packageId)
                .mentorId(mentorId)
                .name("Career Planning")
                .isActive(true)
                .build();

        when(mentorService.createPackage(eq(mentorId), eq(request))).thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<MentorDto.ServicePackageResponse>> result =
                controller.createPackage(principal, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(packageId, result.getBody().getData().getId());
    }

    @Test
    void updateCurriculum_shouldDelegateToServiceAndReturnUpdatedCurriculum() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        CurriculumItemRequest request = new CurriculumItemRequest();
        request.setTitle("Updated session");
        request.setDescription("Updated description");
        request.setOrderIndex(2);
        request.setDuration(75);

        CurriculumItemResponse response = CurriculumItemResponse.builder()
                .id(curriculumId)
                .packageVersionId(versionId)
                .title("Updated session")
                .description("Updated description")
                .orderIndex(2)
                .duration(75)
                .build();

        when(mentorService.updateCurriculumItem(eq(mentorId), eq(packageId), eq(versionId), eq(curriculumId), eq(request)))
                .thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<CurriculumItemResponse>> result =
                controller.updateCurriculum(principal, packageId, versionId, curriculumId, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(curriculumId, result.getBody().getData().getId());
        assertEquals("Updated session", result.getBody().getData().getTitle());
    }

    @Test
    void createPackageVersion_shouldDelegateToServiceAndReturnUpdatedPackage() {
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
        CreateServicePackageVersionRequest request = new CreateServicePackageVersionRequest();
        request.setPrice(new java.math.BigDecimal("150.00"));
        request.setDuration(4);
        request.setDeliveryType("ONLINE");

        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(packageId)
                .mentorId(mentorId)
                .name("Career Planning")
                .isActive(true)
                .build();

        when(mentorService.createPackageVersion(eq(mentorId), eq(packageId), eq(request))).thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<MentorDto.ServicePackageResponse>> result =
                controller.createPackageVersion(principal, packageId, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(packageId, result.getBody().getData().getId());
    }

    @Test
    void getPackageVersions_shouldDelegateToServiceAndReturnPagedVersions() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        PageRequest pageable = PageRequest.of(0, 5);
        MentorDto.ServicePackageVersionResponse versionResponse = MentorDto.ServicePackageVersionResponse.builder()
                .id(versionId)
                .price(new java.math.BigDecimal("150.00"))
                .duration(4)
                .deliveryType("ONLINE")
                .isDefault(true)
                .build();

        when(mentorService.getPackageVersions(eq(mentorId), eq(packageId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(versionResponse), pageable, 1));

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<Page<MentorDto.ServicePackageVersionResponse>>> result =
                controller.getPackageVersions(principal, packageId, pageable);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getData().getTotalElements());
        assertEquals(versionId, result.getBody().getData().getContent().get(0).getId());
    }

    @Test
    void getPackageVersion_shouldDelegateToServiceAndReturnVersionDetail() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        MentorDto.ServicePackageVersionResponse response = MentorDto.ServicePackageVersionResponse.builder()
                .id(versionId)
                .price(new java.math.BigDecimal("150.00"))
                .duration(4)
                .deliveryType("ONLINE")
                .isDefault(true)
                .build();

        when(mentorService.getPackageVersion(eq(mentorId), eq(packageId), eq(versionId))).thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<MentorDto.ServicePackageVersionResponse>> result =
                controller.getPackageVersion(principal, packageId, versionId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(versionId, result.getBody().getData().getId());
    }

    @Test
    void createCurriculum_shouldDelegateToServiceAndReturnCreatedCurriculum() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        CurriculumItemRequest request = new CurriculumItemRequest();
        request.setTitle("Session 1");
        request.setDescription("Intro");
        request.setOrderIndex(1);
        request.setDuration(60);
        CurriculumItemResponse response = CurriculumItemResponse.builder()
                .id(curriculumId)
                .packageVersionId(versionId)
                .title("Session 1")
                .orderIndex(1)
                .duration(60)
                .build();

        when(mentorService.addCurriculumItem(eq(mentorId), eq(packageId), eq(versionId), eq(request)))
                .thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<CurriculumItemResponse>> result =
                controller.createCurriculum(principal, packageId, versionId, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(curriculumId, result.getBody().getData().getId());
    }

    @Test
    void getCurriculums_shouldDelegateToServiceAndReturnPagedCurriculums() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );
        PageRequest pageable = PageRequest.of(0, 5);
        CurriculumItemResponse response = CurriculumItemResponse.builder()
                .id(UUID.randomUUID())
                .packageVersionId(versionId)
                .title("Session 1")
                .orderIndex(1)
                .duration(60)
                .build();

        when(mentorService.listCurriculum(eq(mentorId), eq(packageId), eq(versionId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<Page<CurriculumItemResponse>>> result =
                controller.getCurriculums(principal, packageId, versionId, pageable);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getData().getTotalElements());
    }

    @Test
    void updatePackage_shouldDelegateToServiceAndReturnUpdatedPackage() {
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
        UpdateServicePackageRequest request = new UpdateServicePackageRequest();
        request.setName("Updated package");
        request.setDescription("Updated description");

        MentorDto.ServicePackageResponse response = MentorDto.ServicePackageResponse.builder()
                .id(packageId)
                .mentorId(mentorId)
                .name("Updated package")
                .description("Updated description")
                .isActive(true)
                .build();

        when(mentorService.updatePackage(eq(mentorId), eq(packageId), eq(request))).thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<MentorDto.ServicePackageResponse>> result =
                controller.updatePackage(principal, packageId, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(packageId, result.getBody().getData().getId());
        assertEquals("Updated package", result.getBody().getData().getName());
    }

    @Test
    void togglePackage_shouldDelegateToServiceAndReturnUpdatedPackage() {
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

        when(mentorService.togglePackage(eq(mentorId), eq(packageId))).thenReturn(response);

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<MentorDto.ServicePackageResponse>> result = controller.togglePackage(principal, packageId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(packageId, result.getBody().getData().getId());
        assertEquals(false, result.getBody().getData().getIsActive());
    }

    @Test
    void deleteCurriculum_shouldDelegateToServiceAndReturnSuccessMessage() {
        UUID mentorId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID curriculumId = UUID.randomUUID();
        CustomUserPrincipal principal = new CustomUserPrincipal(
                mentorId,
                "mentor@example.com",
                "hashed",
                List.of("MENTOR"),
                List.of(),
                true
        );

        ServicePackageController controller = new ServicePackageController(mentorService);
        ResponseEntity<ApiResponse<Void>> result =
                controller.deleteCurriculum(principal, packageId, versionId, curriculumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Xoa curriculum thanh cong", result.getBody().getMessage());
    }
}
