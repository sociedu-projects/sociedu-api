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

    @Query(
            value = """
                    SELECT *
                    FROM mentor_profiles m
                    WHERE m.verification_status = :status
                      AND (:keyword IS NULL
                           OR m.headline ILIKE CONCAT('%', :keyword, '%')
                           OR m.expertise ILIKE CONCAT('%', :keyword, '%'))
                      AND (:minPrice IS NULL OR m.base_price >= :minPrice)
                      AND (:maxPrice IS NULL OR m.base_price <= :maxPrice)
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM mentor_profiles m
                    WHERE m.verification_status = :status
                      AND (:keyword IS NULL
                           OR m.headline ILIKE CONCAT('%', :keyword, '%')
                           OR m.expertise ILIKE CONCAT('%', :keyword, '%'))
                      AND (:minPrice IS NULL OR m.base_price >= :minPrice)
                      AND (:maxPrice IS NULL OR m.base_price <= :maxPrice)
                    """,
            nativeQuery = true
    )
    Page<MentorProfile> searchByStatusAndFilters(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
