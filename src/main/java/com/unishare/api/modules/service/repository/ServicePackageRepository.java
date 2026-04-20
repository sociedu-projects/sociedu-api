package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
