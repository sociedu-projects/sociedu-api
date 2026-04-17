package com.unishare.api.modules.service.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.constants.MentorVerificationStatuses;
import com.unishare.api.modules.service.dto.MentorDto;
import com.unishare.api.modules.service.entity.MentorProfile;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.entity.PackageCurriculum;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;
import com.unishare.api.modules.service.exception.ServiceErrorCode;
import com.unishare.api.modules.service.repository.MentorProfileRepository;
import com.unishare.api.modules.service.repository.PackageCurriculumRepository;
import com.unishare.api.modules.service.repository.ServicePackageRepository;
import com.unishare.api.modules.service.repository.ServicePackageVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplTest {

    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private ServicePackageRepository servicePackageRepository;
    @Mock
    private ServicePackageVersionRepository servicePackageVersionRepository;
    @Mock
    private PackageCurriculumRepository packageCurriculumRepository;

    @InjectMocks
    private MentorServiceImpl mentorService;

    private UUID mentorId;
    private UUID packageId;
    private UUID versionId;

    @BeforeEach
    void setUp() {
        mentorId = UUID.randomUUID();
        packageId = UUID.randomUUID();
        versionId = UUID.randomUUID();
    }

    @Test
    void createPackage_whenValidRequest_shouldCreatePackageDefaultVersionAndCurriculums() {
        CreateServicePackageRequest request = validRequest();

        when(servicePackageRepository.save(any(ServicePackage.class))).thenAnswer(invocation -> {
            ServicePackage servicePackage = invocation.getArgument(0);
            servicePackage.setId(packageId);
            return servicePackage;
        });
        when(servicePackageVersionRepository.save(any(ServicePackageVersion.class))).thenAnswer(invocation -> {
            ServicePackageVersion version = invocation.getArgument(0);
            version.setId(versionId);
            return version;
        });
        when(servicePackageVersionRepository.findByPackageId(packageId)).thenReturn(List.of(savedVersion()));
        when(packageCurriculumRepository.findByPackageVersionIdOrderByOrderIndexAsc(versionId))
                .thenReturn(savedCurriculums());

        MentorDto.ServicePackageResponse response = mentorService.createPackage(mentorId, request);

        ArgumentCaptor<ServicePackageVersion> versionCaptor = ArgumentCaptor.forClass(ServicePackageVersion.class);
        verify(servicePackageVersionRepository).save(versionCaptor.capture());
        assertTrue(versionCaptor.getValue().getIsDefault());
        assertEquals(packageId, versionCaptor.getValue().getPackageId());

        ArgumentCaptor<List<PackageCurriculum>> curriculumCaptor = ArgumentCaptor.forClass(List.class);
        verify(packageCurriculumRepository).saveAll(curriculumCaptor.capture());
        assertEquals(2, curriculumCaptor.getValue().size());
        assertTrue(curriculumCaptor.getValue().stream().allMatch(item -> versionId.equals(item.getPackageVersionId())));

        assertEquals(packageId, response.getId());
        assertEquals(1, response.getVersions().size());
        assertTrue(response.getVersions().get(0).getIsDefault());
        assertEquals(2, response.getVersions().get(0).getCurriculums().size());
        assertEquals(List.of(1, 2), response.getVersions().get(0).getCurriculums().stream()
                .map(MentorDto.CurriculumItemResponse::getOrderIndex)
                .toList());
    }

    @Test
    void createPackage_whenCurriculumOrderIndexDuplicated_shouldThrowBusinessException() {
        CreateServicePackageRequest request = validRequest();
        request.getCurriculums().get(1).setOrderIndex(1);

        AppException exception = assertThrows(AppException.class, () -> mentorService.createPackage(mentorId, request));

        assertSame(ServiceErrorCode.DUPLICATE_CURRICULUM_ORDER_INDEX, exception.getExceptionCode());
    }

    @Test
    void createPackage_shouldPersistExactlyOneDefaultVersion() {
        when(servicePackageRepository.save(any(ServicePackage.class))).thenAnswer(invocation -> {
            ServicePackage servicePackage = invocation.getArgument(0);
            servicePackage.setId(packageId);
            return servicePackage;
        });
        when(servicePackageVersionRepository.save(any(ServicePackageVersion.class))).thenAnswer(invocation -> {
            ServicePackageVersion version = invocation.getArgument(0);
            version.setId(versionId);
            return version;
        });
        when(servicePackageVersionRepository.findByPackageId(packageId)).thenReturn(List.of(savedVersion()));
        when(packageCurriculumRepository.findByPackageVersionIdOrderByOrderIndexAsc(versionId))
                .thenReturn(savedCurriculums());

        mentorService.createPackage(mentorId, validRequest());

        ArgumentCaptor<ServicePackageVersion> versionCaptor = ArgumentCaptor.forClass(ServicePackageVersion.class);
        verify(servicePackageVersionRepository).save(versionCaptor.capture());
        assertNotNull(versionCaptor.getValue());
        assertTrue(versionCaptor.getValue().getIsDefault());
        assertFalse(Boolean.FALSE.equals(versionCaptor.getValue().getIsDefault()));
    }

    @Test
    void getAllVerifiedMentors_whenPaged_shouldUseVerifiedStatusAndMapPage() {
        MentorProfile profile = new MentorProfile();
        profile.setUserId(mentorId);
        profile.setHeadline("Career mentor");
        profile.setExpertise("Product");
        profile.setVerificationStatus("verified");

        PageRequest pageable = PageRequest.of(0, 5);
        when(mentorProfileRepository.findByVerificationStatus("verified", pageable))
                .thenReturn(new PageImpl<>(List.of(profile), pageable, 1));
        when(servicePackageRepository.findByMentorId(mentorId)).thenReturn(Collections.emptyList());

        Page<MentorDto.MentorProfileResponse> response = mentorService.getAllVerifiedMentors(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(mentorId, response.getContent().get(0).getUserId());
        assertEquals("verified", response.getContent().get(0).getVerificationStatus());
    }

    @Test
    void getMentorProfile_shouldReturnProfileOnlyWithoutEmbeddedPackages() {
        MentorProfile profile = new MentorProfile();
        profile.setUserId(mentorId);
        profile.setHeadline("Career mentor");
        profile.setExpertise("Product");
        profile.setVerificationStatus("verified");

        when(mentorProfileRepository.findById(mentorId)).thenReturn(java.util.Optional.of(profile));

        MentorDto.MentorProfileResponse response = mentorService.getMentorProfile(mentorId);

        assertEquals(mentorId, response.getUserId());
        assertEquals("Career mentor", response.getHeadline());
        assertEquals("verified", response.getVerificationStatus());
        assertEquals(null, response.getPackages());
        verify(servicePackageRepository, never()).findByMentorId(mentorId);
    }

    @Test
    void createOrUpdateProfile_whenProfileDoesNotExist_shouldCreatePendingProfile() {
        MentorDto.MentorProfileRequest request = new MentorDto.MentorProfileRequest();
        request.setHeadline("Career mentor");
        request.setExpertise("Product");
        request.setBasePrice(new BigDecimal("100.00"));

        when(mentorProfileRepository.findById(mentorId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(savedProfile(MentorVerificationStatuses.PENDING)));
        when(mentorProfileRepository.save(any(MentorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MentorDto.MentorProfileResponse response = mentorService.createOrUpdateProfile(mentorId, request);

        ArgumentCaptor<MentorProfile> profileCaptor = ArgumentCaptor.forClass(MentorProfile.class);
        verify(mentorProfileRepository).save(profileCaptor.capture());
        assertEquals(mentorId, profileCaptor.getValue().getUserId());
        assertEquals(MentorVerificationStatuses.PENDING, profileCaptor.getValue().getVerificationStatus());
        assertEquals("Career mentor", response.getHeadline());
        assertEquals(MentorVerificationStatuses.PENDING, response.getVerificationStatus());
    }

    @Test
    void createOrUpdateProfile_whenProfileExists_shouldPreserveVerificationStatus() {
        MentorProfile existingProfile = savedProfile(MentorVerificationStatuses.VERIFIED);
        existingProfile.setHeadline("Old headline");
        existingProfile.setExpertise("Old expertise");

        MentorDto.MentorProfileRequest request = new MentorDto.MentorProfileRequest();
        request.setHeadline("New headline");
        request.setExpertise("New expertise");
        request.setBasePrice(new BigDecimal("200.00"));

        when(mentorProfileRepository.findById(mentorId))
                .thenReturn(Optional.of(existingProfile))
                .thenReturn(Optional.of(savedProfile(MentorVerificationStatuses.VERIFIED)));
        when(mentorProfileRepository.save(any(MentorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MentorDto.MentorProfileResponse response = mentorService.createOrUpdateProfile(mentorId, request);

        ArgumentCaptor<MentorProfile> profileCaptor = ArgumentCaptor.forClass(MentorProfile.class);
        verify(mentorProfileRepository).save(profileCaptor.capture());
        assertEquals(MentorVerificationStatuses.VERIFIED, profileCaptor.getValue().getVerificationStatus());
        assertEquals("New headline", profileCaptor.getValue().getHeadline());
        assertEquals("New expertise", profileCaptor.getValue().getExpertise());
        assertEquals(MentorVerificationStatuses.VERIFIED, response.getVerificationStatus());
    }

    @Test
    void getMentorPackages_whenPaged_shouldReturnOnlyMappedActivePackages() {
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setId(packageId);
        servicePackage.setMentorId(mentorId);
        servicePackage.setName("Career Planning");
        servicePackage.setDescription("Package description");
        servicePackage.setIsActive(true);

        PageRequest pageable = PageRequest.of(0, 5);
        when(servicePackageRepository.findByMentorIdAndIsActiveTrue(mentorId, pageable))
                .thenReturn(new PageImpl<>(List.of(servicePackage), pageable, 1));
        when(servicePackageVersionRepository.findByPackageId(packageId)).thenReturn(List.of(savedVersion()));
        when(packageCurriculumRepository.findByPackageVersionIdOrderByOrderIndexAsc(versionId))
                .thenReturn(savedCurriculums());

        Page<MentorDto.ServicePackageResponse> response = mentorService.getMentorPackages(mentorId, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("Career Planning", response.getContent().get(0).getName());
        assertEquals(1, response.getContent().get(0).getVersions().size());
    }

    private CreateServicePackageRequest validRequest() {
        CreateServicePackageRequest request = new CreateServicePackageRequest();
        request.setName("Career Planning");
        request.setDescription("Package description");
        request.setPrice(new BigDecimal("120.00"));
        request.setDuration(3);
        request.setDeliveryType("ONLINE");

        CreateServicePackageRequest.CurriculumRequest first = new CreateServicePackageRequest.CurriculumRequest();
        first.setTitle("Session 1");
        first.setDescription("Intro");
        first.setOrderIndex(1);
        first.setDuration(60);

        CreateServicePackageRequest.CurriculumRequest second = new CreateServicePackageRequest.CurriculumRequest();
        second.setTitle("Session 2");
        second.setDescription("Deep dive");
        second.setOrderIndex(2);
        second.setDuration(90);

        request.setCurriculums(List.of(first, second));
        return request;
    }

    private MentorProfile savedProfile(String verificationStatus) {
        MentorProfile profile = new MentorProfile();
        profile.setUserId(mentorId);
        profile.setHeadline("Career mentor");
        profile.setExpertise("Product");
        profile.setBasePrice(new BigDecimal("100.00"));
        profile.setVerificationStatus(verificationStatus);
        return profile;
    }

    private ServicePackageVersion savedVersion() {
        ServicePackageVersion version = new ServicePackageVersion();
        version.setId(versionId);
        version.setPackageId(packageId);
        version.setPrice(new BigDecimal("120.00"));
        version.setDuration(3);
        version.setDeliveryType("ONLINE");
        version.setIsDefault(true);
        return version;
    }

    private List<PackageCurriculum> savedCurriculums() {
        PackageCurriculum first = new PackageCurriculum();
        first.setId(UUID.randomUUID());
        first.setPackageVersionId(versionId);
        first.setTitle("Session 1");
        first.setDescription("Intro");
        first.setOrderIndex(1);
        first.setDuration(60);

        PackageCurriculum second = new PackageCurriculum();
        second.setId(UUID.randomUUID());
        second.setPackageVersionId(versionId);
        second.setTitle("Session 2");
        second.setDescription("Deep dive");
        second.setOrderIndex(2);
        second.setDuration(90);

        return List.of(first, second);
    }
}
