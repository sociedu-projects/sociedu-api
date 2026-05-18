package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.MentorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

        List<MentorProfile> findByVerificationStatus(String status);

        Page<MentorProfile> findByVerificationStatus(String status, Pageable pageable);

        @Query(value = """
                        SELECT *
                        FROM mentor_profiles m
                        WHERE m.verification_status = :status
                          AND (CAST(:keyword AS text) IS NULL
                               OR m.headline ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                               OR m.expertise ILIKE CONCAT('%', CAST(:keyword AS text), '%'))
                          AND (CAST(:minPrice AS numeric) IS NULL OR m.base_price >= CAST(:minPrice AS numeric))
                          AND (CAST(:maxPrice AS numeric) IS NULL OR m.base_price <= CAST(:maxPrice AS numeric))
                        """, countQuery = """
                        SELECT COUNT(*)
                        FROM mentor_profiles m
                        WHERE m.verification_status = :status
                          AND (CAST(:keyword AS text) IS NULL
                               OR m.headline ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                               OR m.expertise ILIKE CONCAT('%', CAST(:keyword AS text), '%'))
                          AND (CAST(:minPrice AS numeric) IS NULL OR m.base_price >= CAST(:minPrice AS numeric))
                          AND (CAST(:maxPrice AS numeric) IS NULL OR m.base_price <= CAST(:maxPrice AS numeric))
                        """, nativeQuery = true)
        Page<MentorProfile> searchByStatusAndFilters(
                        @Param("status") String status,
                        @Param("keyword") String keyword,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        Pageable pageable);
}
