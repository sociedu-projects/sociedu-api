package com.unishare.api.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutRequest {

    /** service_package_versions.id */
    @NotNull
    private UUID servicePackageVersionId;

    /** Mô tả hiển thị trên VNPay (tuỳ chọn) */
    private String orderInfo;
}
