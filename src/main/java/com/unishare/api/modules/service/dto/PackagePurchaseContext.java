package com.unishare.api.modules.service.dto;

import java.util.UUID;

public record PackagePurchaseContext(
        UUID mentorId,
        UUID packageId,
        UUID servicePackageVersionId
) {}
