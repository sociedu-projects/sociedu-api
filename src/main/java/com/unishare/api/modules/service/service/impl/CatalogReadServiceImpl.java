package com.unishare.api.modules.service.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.service.dto.PackagePurchaseContext;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;
import com.unishare.api.modules.service.exception.ServiceErrorCode;
import com.unishare.api.modules.service.repository.ServicePackageRepository;
import com.unishare.api.modules.service.repository.ServicePackageVersionRepository;
import com.unishare.api.modules.service.service.CatalogReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogReadServiceImpl implements CatalogReadService {

    private final ServicePackageVersionRepository versionRepository;
    private final ServicePackageRepository packageRepository;

    @Override
    @Transactional(readOnly = true)
    public ServicePackageVersion requireActiveVersion(UUID servicePackageVersionId) {
        ServicePackageVersion v = versionRepository.findById(servicePackageVersionId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND));
        ServicePackage pkg = packageRepository.findById(v.getPackageId())
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND));
        if (!Boolean.TRUE.equals(pkg.getIsActive())) {
            throw new AppException(ServiceErrorCode.PACKAGE_INACTIVE);
        }
        return v;
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackage getPackage(UUID packageId) {
        return packageRepository.findById(packageId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PackagePurchaseContext resolvePurchaseContext(UUID servicePackageVersionId) {
        ServicePackageVersion v = versionRepository.findById(servicePackageVersionId)
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND));
        ServicePackage p = packageRepository.findById(v.getPackageId())
                .orElseThrow(() -> new AppException(ServiceErrorCode.SERVICE_VERSION_NOT_FOUND));
        return new PackagePurchaseContext(p.getMentorId(), p.getId(), v.getId());
    }
}
