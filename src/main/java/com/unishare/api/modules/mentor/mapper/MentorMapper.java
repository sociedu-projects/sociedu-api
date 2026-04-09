package com.unishare.api.modules.mentor.mapper;

import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.entity.AvailabilitySlot;
import com.unishare.api.modules.mentor.entity.ExpertiseCategory;
import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.mentor.entity.ServicePackage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MentorMapper {

        // ======================== MENTOR PROFILE ========================

        public MentorListResponse toListResponse(MentorProfile profile,
                        String fullName,
                        String avatarUrl,
                        List<ServicePackage> previewPackages) {
                return MentorListResponse.builder()
                                .userId(profile.getUserId())
                                .fullName(fullName)
                                .avatarUrl(avatarUrl)
                                .headline(profile.getHeadline())
                                .expertise(profile.getExpertise())
                                .basePrice(profile.getBasePrice())
                                .ratingAvg(profile.getRatingAvg())
                                .sessionsCompleted(profile.getSessionsCompleted())
                                .previewPackages(previewPackages.stream()
                                                .map(this::toPackageResponse)
                                                .collect(Collectors.toList()))
                                .build();
        }

        public MentorProfileResponse toProfileResponse(MentorProfile profile,
                        String fullName,
                        String avatarUrl,
                        String bio,
                        List<ServicePackage> packages,
                        List<AvailabilitySlot> upcomingSlots) {
                return MentorProfileResponse.builder()
                                .userId(profile.getUserId())
                                .fullName(fullName)
                                .avatarUrl(avatarUrl)
                                .bio(bio)
                                .headline(profile.getHeadline())
                                .expertise(profile.getExpertise())
                                .basePrice(profile.getBasePrice())
                                .ratingAvg(profile.getRatingAvg())
                                .sessionsCompleted(profile.getSessionsCompleted())
                                .verificationStatus(profile.getVerificationStatus())
                                .packages(packages.stream()
                                                .map(this::toPackageResponse)
                                                .collect(Collectors.toList()))
                                .availabilityPreview(upcomingSlots.stream()
                                                .map(this::toSlotResponse)
                                                .collect(Collectors.toList()))
                                .build();
        }

        // ======================== SERVICE PACKAGES ========================

        public ServicePackageResponse toPackageResponse(ServicePackage pkg) {
                return ServicePackageResponse.builder()
                                .id(pkg.getId())
                                .mentorId(pkg.getMentorId())
                                .name(pkg.getName())
                                .description(pkg.getDescription())
                                .duration(pkg.getDuration())
                                .price(pkg.getPrice())
                                .deliveryType(pkg.getDeliveryType())
                                .status(pkg.getStatus())
                                .createdAt(pkg.getCreatedAt())
                                .updatedAt(pkg.getUpdatedAt())
                                .build();
        }

        // ======================== AVAILABILITY SLOTS ========================

        public AvailabilitySlotResponse toSlotResponse(AvailabilitySlot slot) {
                return AvailabilitySlotResponse.builder()
                                .id(slot.getId())
                                .mentorId(slot.getMentorId())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .status(slot.getStatus())
                                .recurrence(slot.getRecurrence())
                                .timezone(slot.getTimezone())
                                .capacity(slot.getCapacity())
                                .build();
        }

        // ======================== EXPERTISE CATEGORIES ========================

        public ExpertiseCategoryResponse toCategoryResponse(ExpertiseCategory category,
                        List<ExpertiseCategory> allCategories) {
                List<ExpertiseCategoryResponse> children = allCategories.stream()
                                .filter(c -> category.getId().equals(c.getParentId()))
                                .map(c -> toCategoryResponse(c, allCategories))
                                .collect(Collectors.toList());

                return ExpertiseCategoryResponse.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .parentId(category.getParentId())
                                .children(children.isEmpty() ? null : children)
                                .build();
        }

        public List<ExpertiseCategoryResponse> toCategoryTree(List<ExpertiseCategory> allCategories) {
                return allCategories.stream()
                                .filter(c -> c.getParentId() == null)
                                .map(c -> toCategoryResponse(c, allCategories))
                                .collect(Collectors.toList());
        }
}
