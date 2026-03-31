package com.unishare.api.modules.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private String itemType;
    private Long itemId;
    private BigDecimal price;
}
