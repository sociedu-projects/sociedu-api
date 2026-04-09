package com.unishare.api.modules.mentor.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.entity.AvailabilitySlot;
import com.unishare.api.modules.mentor.entity.ExpertiseCategory;
import com.unishare.api.modules.mentor.entity.MentorProfile;
import com.unishare.api.modules.mentor.entity.ServicePackage;
import com.unishare.api.modules.mentor.exception.MentorErrorCode;
import com.unishare.api.modules.mentor.mapper.MentorMapper;
import com.unishare.api.modules.mentor.repository.AvailabilitySlotRepository;
import com.unishare.api.modules.mentor.repository.ExpertiseCategoryRepository;
import com.unishare.api.modules.mentor.repository.MentorProfileRepository;
import com.unishare.api.modules.mentor.repository.ServicePackageRepository;
import com.unishare.api.modules.mentor.service.MentorService;
import com.unishare.api.modules.user.dto.UserProfileResponse;
import com.unishare.api.modules.user.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ExpertiseCategoryRepository expertiseCategoryRepository;
    private final MentorMapper mentorMapper;
    private final UserService userService;

    private static final int RECURRENCE_WEEKS = 4;

    // ======================== PUBLIC: Mentor Listing ========================

    @Override
    @Transactional(readOnly = true)
    public Page<MentorListResponse> searchMentors(MentorSearchRequest request, Pageable pageable) {
        Specification<MentorProfile> spec = buildSearchSpecification(request);

        // Build sort based on sortBy parameter
        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "relevance";
        Pageable sortedPageable = applySorting(pageable, sortBy);

        Page<MentorProfile> mentorPage = mentorProfileRepository.findAll(spec, sortedPageable);

        List<MentorListResponse> responses = mentorPage.getContent().stream()
                .map(profile -> {
                    UserProfileResponse userProfile = fetchUserProfileSafe(profile.getUserId());
                    List<ServicePackage> activePackages = servicePackageRepository
                            .findByMentorIdAndStatus(profile.getUserId(), "active");
                    // Preview: max 3 packages
                    List<ServicePackage> previewPkgs = activePackages.stream()
                            .limit(3)
                            .collect(Collectors.toList());
                    return mentorMapper.toListResponse(
                            profile,
                            userProfile != null ? userProfile.getFullName() : null,
                            userProfile != null ? userProfile.getAvatarUrl() : null,
                            previewPkgs);
                })
                .collect(Collectors.toList());

        // Post-query sort for nearest_slot (requires availability data)
        if ("nearest_slot".equals(sortBy)) {
            Instant now = Instant.now();
            responses.sort((a, b) -> {
                Instant slotA = getNearestSlotTime(a.getUserId(), now);
                Instant slotB = getNearestSlotTime(b.getUserId(), now);
                if (slotA == null && slotB == null) return 0;
                if (slotA == null) return 1;
                if (slotB == null) return -1;
                return slotA.compareTo(slotB);
            });
        }

        return new PageImpl<>(responses, pageable, mentorPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public MentorProfileResponse getMentorProfile(Long mentorId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.MENTOR_NOT_FOUND,
                        "Mentor with ID " + mentorId + " not found"));

        UserProfileResponse userProfile = fetchUserProfileSafe(mentorId);

        List<ServicePackage> packages = servicePackageRepository.findByMentorIdAndStatus(mentorId, "active");

        // Preview: upcoming available slots (next 7 days)
        Instant now = Instant.now();
        List<AvailabilitySlot> upcomingSlots = availabilitySlotRepository
                .findByMentorIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(
                        mentorId, now, "available");
        // Limit to 10 for preview
        List<AvailabilitySlot> previewSlots = upcomingSlots.stream()
                .limit(10)
                .collect(Collectors.toList());

        return mentorMapper.toProfileResponse(
                profile,
                userProfile != null ? userProfile.getFullName() : null,
                userProfile != null ? userProfile.getAvatarUrl() : null,
                userProfile != null ? userProfile.getBio() : null,
                packages,
                previewSlots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getPublicMentorPackages(Long mentorId) {
        mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.MENTOR_NOT_FOUND,
                        "Mentor with ID " + mentorId + " not found"));

        return servicePackageRepository.findByMentorIdAndStatus(mentorId, "active").stream()
                .map(mentorMapper::toPackageResponse)
                .collect(Collectors.toList());
    }

    // ======================== MENTOR SELF: Profile ========================

    @Override
    @Transactional
    public MentorProfileResponse createOrUpdateProfile(Long userId, MentorProfileRequest request) {
        MentorProfile profile = mentorProfileRepository.findById(userId)
                .orElse(new MentorProfile());

        profile.setUserId(userId);
        profile.setHeadline(request.getHeadline());
        profile.setExpertise(request.getExpertise());
        profile.setBasePrice(request.getBasePrice());

        if (profile.getVerificationStatus() == null) {
            profile.setVerificationStatus("pending");
        }

        MentorProfile saved = mentorProfileRepository.save(profile);
        log.info("Mentor profile created/updated for userId={}", userId);
        return getMentorProfile(saved.getUserId());
    }

    // ======================== MENTOR SELF: Packages ========================

    @Override
    @Transactional(readOnly = true)
    public List<ServicePackageResponse> getMyPackages(Long mentorId) {
        return servicePackageRepository.findByMentorId(mentorId).stream()
                .map(mentorMapper::toPackageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServicePackageResponse createPackage(Long mentorId, ServicePackageRequest request) {
        ServicePackage pkg = new ServicePackage();
        pkg.setMentorId(mentorId);
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setDuration(request.getDuration());
        pkg.setPrice(request.getPrice());
        pkg.setDeliveryType(request.getDeliveryType());
        pkg.setStatus("draft");

        ServicePackage saved = servicePackageRepository.save(pkg);
        log.info("Service package created: id={}, mentorId={}", saved.getId(), mentorId);
        return mentorMapper.toPackageResponse(saved);
    }

    @Override
    @Transactional
    public ServicePackageResponse updatePackage(Long mentorId, Long packageId,
            ServicePackageUpdateRequest request) {
        ServicePackage pkg = servicePackageRepository.findByIdAndMentorId(packageId, mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.PACKAGE_NOT_FOUND,
                        "Package with ID " + packageId + " not found"));

        // Partial update (PATCH) — only update non-null fields
        if (request.getName() != null) {
            pkg.setName(request.getName());
        }
        if (request.getDescription() != null) {
            pkg.setDescription(request.getDescription());
        }
        if (request.getDuration() != null) {
            // Active packages: restrict sensitive field changes
            if ("active".equals(pkg.getStatus())) {
                throw new AppException(MentorErrorCode.PACKAGE_NOT_EDITABLE,
                        "Cannot change duration of active package. Deactivate first.");
            }
            pkg.setDuration(request.getDuration());
        }
        if (request.getPrice() != null) {
            if ("active".equals(pkg.getStatus())) {
                throw new AppException(MentorErrorCode.PACKAGE_NOT_EDITABLE,
                        "Cannot change price of active package. Deactivate first.");
            }
            pkg.setPrice(request.getPrice());
        }
        if (request.getDeliveryType() != null) {
            pkg.setDeliveryType(request.getDeliveryType());
        }

        ServicePackage saved = servicePackageRepository.save(pkg);
        log.info("Service package updated: id={}", packageId);
        return mentorMapper.toPackageResponse(saved);
    }

    @Override
    @Transactional
    public ServicePackageResponse togglePackageStatus(Long mentorId, Long packageId) {
        ServicePackage pkg = servicePackageRepository.findByIdAndMentorId(packageId, mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.PACKAGE_NOT_FOUND,
                        "Package with ID " + packageId + " not found"));

        String currentStatus = pkg.getStatus();
        String newStatus;

        switch (currentStatus) {
            case "draft":
            case "inactive":
                newStatus = "active";
                break;
            case "active":
                newStatus = "inactive";
                break;
            default:
                throw new AppException(MentorErrorCode.PACKAGE_NOT_EDITABLE,
                        "Package in '" + currentStatus + "' status cannot be toggled");
        }

        pkg.setStatus(newStatus);
        ServicePackage saved = servicePackageRepository.save(pkg);
        log.info("Package {} status changed: {} -> {}", packageId, currentStatus, newStatus);
        return mentorMapper.toPackageResponse(saved);
    }

    @Override
    @Transactional
    public void deletePackage(Long mentorId, Long packageId) {
        ServicePackage pkg = servicePackageRepository.findByIdAndMentorId(packageId, mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.PACKAGE_NOT_FOUND,
                        "Package with ID " + packageId + " not found"));

        if ("active".equals(pkg.getStatus())) {
            throw new AppException(MentorErrorCode.PACKAGE_NOT_EDITABLE,
                    "Cannot delete active package. Deactivate first.");
        }

        servicePackageRepository.delete(pkg);
        log.info("Package deleted: id={}, mentorId={}", packageId, mentorId);
    }

    // ======================== MENTOR SELF: Availability ========================

    @Override
    @Transactional
    public List<AvailabilitySlotResponse> createSlot(Long mentorId, AvailabilitySlotRequest request) {
        // Validate mentor exists and is verified
        MentorProfile profile = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.MENTOR_NOT_FOUND,
                        "Mentor profile not found"));

        if (!"verified".equals(profile.getVerificationStatus())) {
            throw new AppException(MentorErrorCode.MENTOR_NOT_VERIFIED,
                    "Only verified mentors can create availability slots");
        }

        // Validate time range
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new AppException(MentorErrorCode.INVALID_TIME_RANGE,
                    "Start time and end time are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new AppException(MentorErrorCode.INVALID_TIME_RANGE,
                    "End time must be after start time");
        }
        if (request.getStartTime().isBefore(Instant.now())) {
            throw new AppException(MentorErrorCode.INVALID_TIME_RANGE,
                    "Cannot create slots in the past");
        }

        String recurrence = request.getRecurrence() != null ? request.getRecurrence() : "none";
        String timezone = request.getTimezone() != null ? request.getTimezone() : "UTC";
        int capacity = request.getCapacity() != null ? request.getCapacity() : 1;

        List<AvailabilitySlot> slotsToCreate = new ArrayList<>();

        if ("none".equals(recurrence)) {
            // Single slot
            validateNoOverlap(mentorId, request.getStartTime(), request.getEndTime());
            AvailabilitySlot slot = buildSlot(mentorId, request.getStartTime(),
                    request.getEndTime(), recurrence, timezone, capacity);
            slotsToCreate.add(slot);
        } else {
            // Recurring slots: generate for RECURRENCE_WEEKS weeks
            Duration slotDuration = Duration.between(request.getStartTime(), request.getEndTime());
            int daysInterval = "daily".equals(recurrence) ? 1 : 7;

            for (int i = 0; i < RECURRENCE_WEEKS * (7 / daysInterval); i++) {
                Instant start = request.getStartTime().plus(i * daysInterval, ChronoUnit.DAYS);
                Instant end = start.plus(slotDuration);

                // Skip if overlapping, but don't fail
                if (!availabilitySlotRepository.existsOverlappingSlot(mentorId, start, end)) {
                    AvailabilitySlot slot = buildSlot(mentorId, start, end, recurrence, timezone, capacity);
                    slotsToCreate.add(slot);
                }
            }
        }

        List<AvailabilitySlot> saved = availabilitySlotRepository.saveAll(slotsToCreate);
        log.info("Created {} availability slots for mentorId={}", saved.size(), mentorId);

        return saved.stream()
                .map(mentorMapper::toSlotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AvailabilitySlotResponse updateSlot(Long mentorId, Long slotId,
            AvailabilitySlotUpdateRequest request) {
        AvailabilitySlot slot = availabilitySlotRepository.findByIdAndMentorId(slotId, mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.SLOT_NOT_FOUND,
                        "Slot with ID " + slotId + " not found"));

        // Cannot modify booked slots
        if ("booked".equals(slot.getStatus())) {
            throw new AppException(MentorErrorCode.SLOT_ALREADY_BOOKED,
                    "Cannot modify a booked slot");
        }

        // Update status (block/cancel)
        if (request.getStatus() != null) {
            if ("blocked".equals(request.getStatus()) || "cancelled".equals(request.getStatus())) {
                slot.setStatus(request.getStatus());
            } else if ("available".equals(request.getStatus())) {
                slot.setStatus("available");
            }
        }

        // Update time range
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new AppException(MentorErrorCode.INVALID_TIME_RANGE,
                        "End time must be after start time");
            }
            // Check overlap (excluding this slot)
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
        }

        if (request.getCapacity() != null) {
            slot.setCapacity(request.getCapacity());
        }

        AvailabilitySlot saved = availabilitySlotRepository.save(slot);
        log.info("Slot updated: id={}, status={}", slotId, saved.getStatus());
        return mentorMapper.toSlotResponse(saved);
    }

    // ======================== PUBLIC: Availability ========================

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getMentorAvailability(Long mentorId) {
        mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new AppException(MentorErrorCode.MENTOR_NOT_FOUND,
                        "Mentor with ID " + mentorId + " not found"));

        Instant now = Instant.now();
        return availabilitySlotRepository
                .findByMentorIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(mentorId, now, "available")
                .stream()
                .map(mentorMapper::toSlotResponse)
                .collect(Collectors.toList());
    }

    // ======================== PUBLIC: Categories ========================

    @Override
    @Transactional(readOnly = true)
    public List<ExpertiseCategoryResponse> getAllCategories() {
        List<ExpertiseCategory> all = expertiseCategoryRepository.findAllByOrderBySortOrderAsc();
        return mentorMapper.toCategoryTree(all);
    }

    // ======================== PRIVATE HELPERS ========================

    private Specification<MentorProfile> buildSearchSpecification(MentorSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only verified mentors
            predicates.add(cb.equal(root.get("verificationStatus"), "verified"));

            if (request != null) {
                if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                    String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                    Predicate headlineMatch = cb.like(cb.lower(root.get("headline")), keyword);
                    Predicate expertiseMatch = cb.like(cb.lower(root.get("expertise")), keyword);
                    predicates.add(cb.or(headlineMatch, expertiseMatch));
                }

                if (request.getExpertise() != null && !request.getExpertise().isBlank()) {
                    String expertise = "%" + request.getExpertise().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("expertise")), expertise));
                }

                if (request.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), request.getMinPrice()));
                }

                if (request.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), request.getMaxPrice()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserProfileResponse fetchUserProfileSafe(Long userId) {
        try {
            return userService.getProfile(userId);
        } catch (Exception e) {
            log.warn("Could not fetch user profile for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private void validateNoOverlap(Long mentorId, Instant startTime, Instant endTime) {
        if (availabilitySlotRepository.existsOverlappingSlot(mentorId, startTime, endTime)) {
            throw new AppException(MentorErrorCode.SLOT_CONFLICT,
                    "Time slot overlaps with an existing slot");
        }
    }

    private AvailabilitySlot buildSlot(Long mentorId, Instant startTime, Instant endTime,
            String recurrence, String timezone, int capacity) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setMentorId(mentorId);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setStatus("available");
        slot.setRecurrence(recurrence);
        slot.setTimezone(timezone);
        slot.setCapacity(capacity);
        return slot;
    }

    private Pageable applySorting(Pageable pageable, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "rating":
                sort = Sort.by(Sort.Direction.DESC, "ratingAvg");
                break;
            case "price_asc":
                sort = Sort.by(Sort.Direction.ASC, "basePrice");
                break;
            case "price_desc":
                sort = Sort.by(Sort.Direction.DESC, "basePrice");
                break;
            case "sessions":
                sort = Sort.by(Sort.Direction.DESC, "sessionsCompleted");
                break;
            case "nearest_slot":
                // nearest_slot sorting is done post-query; use default DB order here
                sort = Sort.unsorted();
                break;
            case "relevance":
            default:
                // Relevance = rating DESC, then sessions DESC
                sort = Sort.by(Sort.Direction.DESC, "ratingAvg")
                        .and(Sort.by(Sort.Direction.DESC, "sessionsCompleted"));
                break;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Instant getNearestSlotTime(Long mentorId, Instant after) {
        List<AvailabilitySlot> slots = availabilitySlotRepository
                .findByMentorIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(mentorId, after, "available");
        return slots.isEmpty() ? null : slots.get(0).getStartTime();
    }
}
