package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, UUID> {
    List<ServicePackage> findByMentorId(UUID mentorId);

    Page<ServicePackage> findByMentorId(UUID mentorId, Pageable pageable);

    Page<ServicePackage> findByMentorIdAndIsActiveTrue(UUID mentorId, Pageable pageable);

    Page<ServicePackage> findByIsActiveTrue(Pageable pageable);

    Optional<ServicePackage> findByIdAndIsActiveTrue(UUID id);

    @Query("""
            SELECT p FROM ServicePackage p
            WHERE p.isActive = true
            AND (:mentorId IS NULL OR p.mentorId = :mentorId)
            AND (:keyword IS NULL
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ServicePackage> searchActivePackages(
            @Param("mentorId") UUID mentorId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            SELECT p FROM ServicePackage p
            WHERE p.mentorId = :mentorId
            AND p.isActive = true
            AND (:keyword IS NULL
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ServicePackage> searchActiveByMentorId(
            @Param("mentorId") UUID mentorId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            SELECT p FROM ServicePackage p
            WHERE p.mentorId = :mentorId
            AND (:keyword IS NULL
                 OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ServicePackage> searchByMentorId(
            @Param("mentorId") UUID mentorId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
