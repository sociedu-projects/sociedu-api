package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

        List<AvailabilitySlot> findByMentorIdAndStatus(Long mentorId, String status);

        List<AvailabilitySlot> findByMentorIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(
                        Long mentorId, Instant after, String status);

        Optional<AvailabilitySlot> findByIdAndMentorId(Long id, Long mentorId);

        @Query("SELECT s FROM AvailabilitySlot s WHERE s.mentorId = :mentorId " +
                        "AND s.status IN :statuses " +
                        "AND s.startTime >= :from AND s.startTime <= :to " +
                        "ORDER BY s.startTime ASC")
        List<AvailabilitySlot> findByMentorIdAndTimeRange(
                        @Param("mentorId") Long mentorId,
                        @Param("from") Instant from,
                        @Param("to") Instant to,
                        @Param("statuses") List<String> statuses);

        @Query("SELECT COUNT(s) > 0 FROM AvailabilitySlot s WHERE s.mentorId = :mentorId " +
                        "AND s.status = 'available' " +
                        "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
        boolean existsOverlappingSlot(
                        @Param("mentorId") Long mentorId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);
}
