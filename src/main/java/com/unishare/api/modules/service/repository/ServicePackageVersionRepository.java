package com.unishare.api.modules.service.repository;

import com.unishare.api.modules.service.entity.ServicePackageVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicePackageVersionRepository extends JpaRepository<ServicePackageVersion, UUID> {
    List<ServicePackageVersion> findByPackageId(UUID packageId);
}
