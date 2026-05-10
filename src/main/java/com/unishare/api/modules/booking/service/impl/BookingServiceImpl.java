package com.unishare.api.modules.booking.service.impl;

import com.unishare.api.common.constants.BookingStatuses;
import com.unishare.api.common.constants.OrderStatuses;
import com.unishare.api.common.constants.SessionStatuses;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.event.BookingCreatedEvent;
import com.unishare.api.infrastructure.event.DomainEventPublisher;
import com.unishare.api.modules.booking.dto.*;
import com.unishare.api.modules.booking.entity.Booking;
import com.unishare.api.modules.booking.entity.BookingSession;
import com.unishare.api.modules.booking.entity.BookingSessionEvidence;
import com.unishare.api.modules.booking.exception.BookingErrorCode;
import com.unishare.api.modules.booking.repository.BookingRepository;
import com.unishare.api.modules.booking.repository.BookingSessionEvidenceRepository;
import com.unishare.api.modules.booking.repository.BookingSessionRepository;
import com.unishare.api.modules.booking.service.BookingService;
import com.unishare.api.modules.file.service.FileService;
import com.unishare.api.modules.order.dto.OrderSnapshot;
import com.unishare.api.modules.order.service.OrderService;
import com.unishare.api.modules.service.dto.PackageCurriculumSeedItem;
import com.unishare.api.modules.service.service.CatalogReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSessionRepository sessionRepository;
    private final BookingSessionEvidenceRepository evidenceRepository;
    private final OrderService orderService;
    private final CatalogReadService catalogReadService;
    private final FileService fileService;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public void ensureBookingForOrder(UUID orderId) {
        OrderSnapshot snap = orderService.getOrderSnapshot(orderId);
        if (!OrderStatuses.PAID.equals(snap.status())) {
            return;
        }
        if (bookingRepository.findByOrderId(orderId).isPresent()) {
            return;
        }
        var ctx = catalogReadService.resolvePurchaseContext(snap.serviceId());
        Booking b = new Booking();
        b.setOrderId(orderId);
        b.setBuyerId(snap.buyerId());
        b.setMentorId(ctx.mentorId());
        b.setPackageId(ctx.packageId());
        b.setStatus(BookingStatuses.SCHEDULED);
        b = bookingRepository.save(b);
        seedSessions(b.getId(), snap.serviceId());
        eventPublisher.publish(new BookingCreatedEvent(b.getId(), orderId, b.getBuyerId(), b.getMentorId()));
    }

    private void seedSessions(UUID bookingId, UUID versionId) {
        List<PackageCurriculumSeedItem> rows = catalogReadService.listCurriculumForVersionOrdered(versionId);
        int i = 0;
        for (PackageCurriculumSeedItem c : rows) {
            BookingSession s = new BookingSession();
            s.setBookingId(bookingId);
            s.setCurriculumId(c.id());
            s.setTitle(c.title());
            s.setScheduledAt(Instant.now().plus(Duration.ofDays(i + 1L)));
            s.setStatus(SessionStatuses.SCHEDULED);
            sessionRepository.save(s);
            i++;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listForBuyer(UUID buyerId) {
        return bookingRepository.findByBuyerId(buyerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> listForMentor(UUID mentorId) {
        return bookingRepository.findByMentorId(mentorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId, UUID userId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND));
        assertAccess(b, userId);
        return toResponse(b);
    }

    @Override
    @Transactional
    public BookingSessionResponse updateSession(UUID bookingId, UUID sessionId, UUID actorUserId, UpdateSessionRequest req) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND));

        boolean isMentor = b.getMentorId().equals(actorUserId);
        boolean isBuyer = b.getBuyerId().equals(actorUserId);

        if (!isMentor && !isBuyer) {
            throw new AppException(BookingErrorCode.BOOKING_ACCESS_DENIED);
        }

        BookingSession s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(BookingErrorCode.SESSION_NOT_FOUND));
        if (!s.getBookingId().equals(bookingId)) {
            throw new AppException(BookingErrorCode.SESSION_NOT_FOUND);
        }

        if (req.getScheduledAt() != null || req.getMeetingUrl() != null) {
            if (!isMentor) {
                throw new AppException(BookingErrorCode.BOOKING_ACCESS_DENIED, "Chỉ Mentor mới được phép cập nhật lịch học và link meeting.");
            }
        }

        if (req.getScheduledAt() != null) {
            if (req.getScheduledAt().isBefore(Instant.now())) {
                throw new AppException(BookingErrorCode.INVALID_SCHEDULE_TIME, "Không thể xếp lịch trong quá khứ.");
            }
            // Simple overlap check (±1 hour buffer)
            Instant start = req.getScheduledAt().minus(Duration.ofHours(1));
            Instant end = req.getScheduledAt().plus(Duration.ofHours(1));
            if (sessionRepository.existsOverlappingSession(b.getMentorId(), s.getId(), start, end)) {
                throw new AppException(BookingErrorCode.INVALID_SCHEDULE_TIME, "Lịch học bị trùng với một buổi học khác của Mentor.");
            }
            s.setScheduledAt(req.getScheduledAt());
        }

        if (req.getMeetingUrl() != null) {
            s.setMeetingUrl(req.getMeetingUrl());
        }

        if (req.getStatus() != null && !s.getStatus().equals(req.getStatus())) {
            try {
                com.unishare.api.modules.booking.policy.SessionStatusTransitionPolicy.validateTransition(s.getStatus(), req.getStatus());
            } catch (IllegalStateException e) {
                throw new AppException(BookingErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
            }
            
            if (SessionStatuses.COMPLETED.equals(req.getStatus())) {
                // Validation: now >= scheduledAt + minimumDuration (e.g. 15 mins)
                if (s.getScheduledAt() != null) {
                    Instant minCompletionTime = s.getScheduledAt().plus(Duration.ofMinutes(15));
                    if (Instant.now().isBefore(minCompletionTime)) {
                        throw new AppException(BookingErrorCode.INVALID_STATE_TRANSITION, "Không thể hoàn thành buổi học trước thời gian tối thiểu.");
                    }
                }
                if (s.getActualStartedAt() == null) {
                    throw new AppException(BookingErrorCode.INVALID_STATE_TRANSITION, "Phải bắt đầu buổi học (IN_PROGRESS) trước khi hoàn thành.");
                }
                
                s.setStatus(SessionStatuses.COMPLETED);
                s.setActualEndedAt(Instant.now());
                s.setCompletedAt(Instant.now());
                
            } else if (com.unishare.api.modules.booking.policy.SessionStatusTransitionPolicy.IN_PROGRESS.equals(req.getStatus())) {
                s.setStatus(req.getStatus());
                s.setActualStartedAt(Instant.now());
                
                if (BookingStatuses.PENDING.equals(b.getStatus()) || BookingStatuses.SCHEDULED.equals(b.getStatus())) {
                    com.unishare.api.modules.booking.policy.BookingStatusTransitionPolicy.validateTransition(b.getStatus(), BookingStatuses.IN_PROGRESS);
                    b.setStatus(BookingStatuses.IN_PROGRESS);
                    bookingRepository.save(b);
                }
            } else if (SessionStatuses.CANCELED.equals(req.getStatus())) {
                s.setStatus(req.getStatus());
                s.setCanceledBy(actorUserId);
                s.setCanceledAt(Instant.now());
                s.setCancelReason("Canceled by user");
            } else {
                s.setStatus(req.getStatus());
            }
        }

        // Auto transition to SCHEDULED if both details are provided and it is still PENDING
        if (s.getScheduledAt() != null && s.getMeetingUrl() != null && SessionStatuses.PENDING.equals(s.getStatus())) {
            try {
                com.unishare.api.modules.booking.policy.SessionStatusTransitionPolicy.validateTransition(s.getStatus(), SessionStatuses.SCHEDULED);
                s.setStatus(SessionStatuses.SCHEDULED);
            } catch (IllegalStateException e) {
                throw new AppException(BookingErrorCode.INVALID_STATE_TRANSITION, e.getMessage());
            }
        }

        sessionRepository.save(s);
        
        // Aggregate completion check after saving session
        if (SessionStatuses.COMPLETED.equals(s.getStatus())) {
            checkAndCompleteBooking(b);
        }
        
        return mapSession(s);
    }

    private void checkAndCompleteBooking(Booking b) {
        long uncompletedCount = sessionRepository.countUncompletedSessionsByBookingId(b.getId());
        if (uncompletedCount == 0 && !BookingStatuses.COMPLETED.equals(b.getStatus())) {
            com.unishare.api.modules.booking.policy.BookingStatusTransitionPolicy.validateTransition(b.getStatus(), BookingStatuses.COMPLETED);
            b.setStatus(BookingStatuses.COMPLETED);
            bookingRepository.save(b);
            
            eventPublisher.publish(new com.unishare.api.common.event.BookingCompletedEvent(
                    b.getId(), b.getMentorId(), b.getBuyerId(), b.getOrderId()));
        }
    }

    @Override
    @Transactional
    public EvidenceResponse addEvidence(UUID bookingId, UUID sessionId, UUID userId, AddEvidenceRequest req) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(BookingErrorCode.BOOKING_NOT_FOUND));
        if (!b.getBuyerId().equals(userId) && !b.getMentorId().equals(userId)) {
            throw new AppException(BookingErrorCode.BOOKING_ACCESS_DENIED);
        }
        BookingSession s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(BookingErrorCode.SESSION_NOT_FOUND));
        if (!s.getBookingId().equals(bookingId)) {
            throw new AppException(BookingErrorCode.SESSION_NOT_FOUND);
        }
        fileService.getFile(req.getFileId(), userId);
        BookingSessionEvidence ev = new BookingSessionEvidence();
        ev.setBookingSessionId(sessionId);
        ev.setUploadedBy(userId);
        ev.setFileId(req.getFileId());
        ev.setDescription(req.getDescription());
        ev = evidenceRepository.save(ev);
        return EvidenceResponse.builder()
                .id(ev.getId())
                .uploadedBy(ev.getUploadedBy())
                .fileId(ev.getFileId())
                .description(ev.getDescription())
                .createdAt(ev.getCreatedAt())
                .build();
    }

    private void assertAccess(Booking b, UUID userId) {
        if (!b.getBuyerId().equals(userId) && !b.getMentorId().equals(userId)) {
            throw new AppException(BookingErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    private BookingResponse toResponse(Booking b) {
        List<BookingSession> sessions = sessionRepository.findByBookingIdOrderByScheduledAtAsc(b.getId());
        return BookingResponse.builder()
                .id(b.getId())
                .orderId(b.getOrderId())
                .buyerId(b.getBuyerId())
                .mentorId(b.getMentorId())
                .packageId(b.getPackageId())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .sessions(sessions.stream().map(this::mapSession).collect(Collectors.toList()))
                .build();
    }

    private BookingSessionResponse mapSession(BookingSession s) {
        List<BookingSessionEvidence> evs = evidenceRepository.findByBookingSessionId(s.getId());
        return BookingSessionResponse.builder()
                .id(s.getId())
                .curriculumId(s.getCurriculumId())
                .title(s.getTitle())
                .scheduledAt(s.getScheduledAt())
                .completedAt(s.getCompletedAt())
                .status(s.getStatus())
                .meetingUrl(s.getMeetingUrl())
                .evidences(evs.stream().map(e -> EvidenceResponse.builder()
                        .id(e.getId())
                        .uploadedBy(e.getUploadedBy())
                        .fileId(e.getFileId())
                        .description(e.getDescription())
                        .createdAt(e.getCreatedAt())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
