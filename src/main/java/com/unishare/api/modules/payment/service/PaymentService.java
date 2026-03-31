package com.unishare.api.modules.payment.service;

import com.unishare.api.modules.payment.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentService - có thể tái sử dụng cho Document orders và Mentor bookings.
 * Bất kỳ module nào cần thanh toán đều inject PaymentService này.
 */
public interface PaymentService {

    /**
     * Tạo URL thanh toán VNPay và PaymentTransaction record (status=pending)
     * @param orderId   ID đơn hàng (từ bất kỳ module nào)
     * @param amount    Số tiền VND
     * @param orderInfo Mô tả đơn hàng hiển thị trên VNPay
     * @param ipAddress IP của client
     * @return PaymentResponse chứa paymentUrl để redirect
     */
    PaymentResponse createPayment(Long orderId, BigDecimal amount, String orderInfo, String ipAddress);

    /**
     * Xử lý IPN/callback từ VNPay sau khi user thanh toán.
     * Verify signature, cập nhật PaymentTransaction status = success/failed.
     * @param params Tất cả query params từ VNPay callback
     * @return true nếu payment thành công và hợp lệ
     */
    boolean handleVNPayCallback(Map<String, String> params);

    /**
     * Kiểm tra thanh toán đơn hàng đã thành công chưa
     */
    boolean isOrderPaid(Long orderId);

    /**
     * Lấy PaymentResponse theo orderId
     */
    PaymentResponse getPaymentByOrderId(Long orderId);
}
