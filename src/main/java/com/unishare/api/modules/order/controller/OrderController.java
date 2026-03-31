package com.unishare.api.modules.order.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.order.dto.OrderRequest;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * Tạo đơn mua tài liệu - trả về VNPay payment URL
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody OrderRequest request,
            HttpServletRequest httpRequest) {
        // Lấy client IP để gắn vào VNPay request
        String clientIp = getClientIp(httpRequest);
        request.setClientIp(clientIp);
        OrderResponse order = orderService.createDocumentOrder(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponse>build()
                        .withHttpStatus(HttpStatus.CREATED)
                        .withData(order)
                        .withMessage("Order created. Please complete payment via paymentUrl."));
    }

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

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip;
    }
}
