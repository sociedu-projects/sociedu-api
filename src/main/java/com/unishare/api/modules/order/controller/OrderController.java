package com.unishare.api.modules.order.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Lấy danh sách đơn hàng của mình
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>build()
                .withData(orderService.getMyOrders(principal.getUserId())));
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<OrderResponse>build()
                .withData(orderService.getOrderById(id, principal.getUserId())));
    }
}

