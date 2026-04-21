package com.unishare.api.modules.mentor_request.repository;

import com.unishare.api.modules.mentor_request.entity.MentorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho đơn apply mentor. Query kèm phân trang + filter cho trang admin.
 */
@Repository
public interface MentorRequestRepository extends JpaRepository<MentorRequest, UUID> {

    Optional<MentorRequest> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndStatusIn(UUID userId, java.util.Collection<String> statuses);

    @Query("""
            SELECT r FROM MentorRequest r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:q IS NULL OR LOWER(r.headline) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<MentorRequest> searchAdmin(@Param("status") String status,
                                    @Param("q") String q,
                                    Pageable pageable);
}
