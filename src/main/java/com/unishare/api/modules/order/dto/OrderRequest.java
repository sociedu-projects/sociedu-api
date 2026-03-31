package com.unishare.api.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    /** ID tài liệu muốn mua */
    @NotNull(message = "documentId is required")
    private Long documentId;

    /** IP của client (để tạo VNPay URL) */
    private String clientIp;
}
