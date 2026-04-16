package com.unishare.api.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class OrderRequest {

    /** ID sản phẩm / dịch vụ muốn mua */
    @NotNull(message = "itemId is required")
    private UUID itemId;

    /** IP của client (để tạo VNPay URL) */
    private String clientIp;
}
