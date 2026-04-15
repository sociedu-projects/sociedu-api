package com.unishare.api.modules.payment.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.common.dto.AppException;
import com.unishare.api.common.dto.CommonErrorCode;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.modules.payment.dto.PaymentResponse;
import com.unishare.api.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments — VNPay")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * VNPay IPN (Instant Payment Notification) - gọi bởi VNPay server
     * Không yêu cầu JWT (VNPay server gọi trực tiếp)
     */
    @Operation(summary = "VNPay IPN")
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<String> vnpayIPN(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN received: txnRef={}", params.get("vnp_TxnRef"));
        try {
            boolean success = paymentService.handleVNPayCallback(params);
            return ResponseEntity.ok(success ? "00|Confirm Success" : "01|Error");
        } catch (Exception e) {
            log.error("VNPay IPN error", e);
            return ResponseEntity.ok("01|Error");
        }
    }

    /**
     * VNPay Return URL - redirect user sau khi thanh toán
     * Trả thông tin cho frontend biết kết quả
     */
    @Operation(summary = "VNPay return URL")
    @GetMapping("/vnpay/return")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay Return: txnRef={}, responseCode={}", params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"));
        boolean success = paymentService.handleVNPayCallback(params);
        UUID orderId = parseOrderIdFromTxnRef(params.get("vnp_TxnRef"));
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>build()
                .withData(payment)
                .withMessage(success ? "Payment successful" : "Payment failed"));
    }

    /**
     * Lấy trạng thái payment theo orderId
     */
    @Operation(summary = "Payment theo orderId")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PAYMENT)")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>build()
                .withData(paymentService.getPaymentByOrderId(orderId)));
    }

    /** txnRef format: orderUuid_timestamp (see {@link com.unishare.api.modules.payment.service.impl.VNPayServiceImpl#createPayment}). */
    private static UUID parseOrderIdFromTxnRef(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            throw new AppException(CommonErrorCode.BAD_REQUEST, "Thiếu vnp_TxnRef");
        }
        int u = txnRef.indexOf('_');
        if (u <= 0) {
            throw new AppException(CommonErrorCode.BAD_REQUEST, "Định dạng vnp_TxnRef không hợp lệ");
        }
        try {
            return UUID.fromString(txnRef.substring(0, u));
        } catch (IllegalArgumentException e) {
            throw new AppException(CommonErrorCode.BAD_REQUEST, "Định dạng vnp_TxnRef không hợp lệ");
        }
    }
}
