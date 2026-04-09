package com.unishare.api.modules.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class AdminDto {

    @Data
    @Builder
    public static class AdminStatsResponse {
        private BigDecimal totalSales;
        private Long orderCount;
        private List<Object> recentOrders; // simplified
        private Long pendingMentorRequests;
    }
}
