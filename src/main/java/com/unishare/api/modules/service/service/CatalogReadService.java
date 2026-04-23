package com.unishare.api.modules.service.service;

import com.unishare.api.modules.service.dto.PackageCurriculumSeedItem;
import com.unishare.api.modules.service.dto.PackagePurchaseContext;
import com.unishare.api.modules.service.entity.ServicePackage;
import com.unishare.api.modules.service.entity.ServicePackageVersion;

import java.util.List;
import java.util.UUID;

/**
 * Đọc catalog cho module khác (order, booking) — không expose repository ngoài package service.
 */
public interface CatalogReadService {

    ServicePackageVersion requireActiveVersion(UUID servicePackageVersionId);

    ServicePackage getPackage(UUID packageId);

    /** mentorId + packageId từ phiên bản gói (đơn đã thanh toán). */
    PackagePurchaseContext resolvePurchaseContext(UUID servicePackageVersionId);

    /** Bài học theo thứ tự trong một phiên bản gói (booking seed sessions). */
    List<PackageCurriculumSeedItem> listCurriculumForVersionOrdered(UUID packageVersionId);
}
