package com.unishare.api.modules.payment.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.payment.dto.PaymentResponse;
import com.unishare.api.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * VNPay IPN (Instant Payment Notification) - gọi bởi VNPay server
     * Không yêu cầu JWT (VNPay server gọi trực tiếp)
     */
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
    @GetMapping("/vnpay/return")
    public ResponseEntity<ApiResponse<PaymentResponse>> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay Return: txnRef={}, responseCode={}", params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"));
        boolean success = paymentService.handleVNPayCallback(params);
        String txnRef = params.get("vnp_TxnRef");
        // txnRef format: orderId_timestamp
        Long orderId = Long.parseLong(txnRef.split("_")[0]);
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>build()
                .withData(payment)
                .withMessage(success ? "Payment successful" : "Payment failed"));
    }

    /**
     * Lấy trạng thái payment theo orderId
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>build()
                .withData(paymentService.getPaymentByOrderId(orderId)));
    }
}
