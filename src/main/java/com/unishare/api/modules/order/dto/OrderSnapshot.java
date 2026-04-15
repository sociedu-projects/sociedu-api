package com.unishare.api.modules.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSnapshot(
        UUID id,
        UUID buyerId,
        UUID serviceId,
        String status,
        BigDecimal totalAmount
) {}
