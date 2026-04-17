package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.PackageCurriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PackageCurriculumRepository extends JpaRepository<PackageCurriculum, UUID> {
    List<PackageCurriculum> findByPackageVersionIdOrderByOrderIndexAsc(UUID packageVersionId);

    Page<PackageCurriculum> findByPackageVersionIdOrderByOrderIndexAsc(UUID packageVersionId, Pageable pageable);

    boolean existsByPackageVersionIdAndOrderIndex(UUID packageVersionId, Integer orderIndex);

    void deleteByPackageVersionId(UUID packageVersionId);
}
