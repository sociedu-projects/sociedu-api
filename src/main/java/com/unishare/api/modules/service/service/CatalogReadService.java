package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.PackagePurchaseContext;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;

import java.util.UUID;

/**
 * Đọc catalog cho module khác (order, booking) — không expose repository ngoài package service.
 */
public interface CatalogReadService {

    ServicePackageVersion requireActiveVersion(UUID servicePackageVersionId);

    ServicePackage getPackage(UUID packageId);

    /** mentorId + packageId từ phiên bản gói (đơn đã thanh toán). */
    PackagePurchaseContext resolvePurchaseContext(UUID servicePackageVersionId);
}
