package com.unishare.api.modules.payment.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.payment.dto.PaymentResponse;
import com.unishare.api.modules.payment.entity.Escrow;
import com.unishare.api.modules.payment.entity.PaymentTransaction;
import com.unishare.api.modules.payment.exception.PaymentErrorCode;
import com.unishare.api.modules.payment.repository.EscrowRepository;
import com.unishare.api.modules.payment.repository.PaymentTransactionRepository;
import com.unishare.api.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EscrowRepository escrowRepository;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Override
    @Transactional
    public PaymentResponse createPayment(Long orderId, BigDecimal amount, String orderInfo, String ipAddress) {
        // Transaction ref = orderId + timestamp để tránh trùng
        String txnRef = orderId + "_" + System.currentTimeMillis();

        // Tạo PaymentTransaction với status pending
        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(orderId);
        txn.setProvider("vnpay");
        txn.setTransactionRef(txnRef);
        txn.setStatus("pending");
        paymentTransactionRepository.save(txn);

        // Tạo Escrow holding
        Escrow escrow = new Escrow();
        escrow.setOrderId(orderId);
        escrow.setAmount(amount);
        escrow.setStatus("holding");
        escrowRepository.save(escrow);

        // Build VNPay payment URL
        String paymentUrl = buildVNPayUrl(txnRef, amount, orderInfo, ipAddress);

        PaymentResponse response = new PaymentResponse();
        response.setId(txn.getId());
        response.setOrderId(orderId);
        response.setProvider("vnpay");
        response.setTransactionRef(txnRef);
        response.setStatus("pending");
        response.setCreatedAt(txn.getCreatedAt());
        response.setPaymentUrl(paymentUrl);
        return response;
    }

    @Override
    @Transactional
    public boolean handleVNPayCallback(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        // Remove hash fields before verifying
        Map<String, String> vnpParams = new TreeMap<>(params);
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        String calculatedHash = hmacSHA512(hashSecret, buildRawData(vnpParams));
        if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
            log.warn("VNPay callback: invalid signature for txnRef={}", params.get("vnp_TxnRef"));
            throw new AppException(PaymentErrorCode.INVALID_SIGNATURE);
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        boolean success = "00".equals(responseCode);

        PaymentTransaction txn = paymentTransactionRepository.findByTransactionRef(txnRef)
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (!"pending".equals(txn.getStatus())) {
            log.warn("VNPay callback: transaction {} already processed", txnRef);
            throw new AppException(PaymentErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        txn.setStatus(success ? "success" : "failed");
        paymentTransactionRepository.save(txn);

        // Cập nhật escrow
        escrowRepository.findByOrderId(txn.getOrderId()).ifPresent(escrow -> {
            escrow.setStatus(success ? "released" : "cancelled");
            escrowRepository.save(escrow);
        });

        log.info("VNPay payment {} for orderId={}: {}", txnRef, txn.getOrderId(), success ? "SUCCESS" : "FAILED");
        return success;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderPaid(Long orderId) {
        return paymentTransactionRepository.findByOrderId(orderId).stream()
                .anyMatch(txn -> "success".equals(txn.getStatus()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        PaymentTransaction txn = paymentTransactionRepository.findByOrderId(orderId).stream()
                .findFirst()
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        PaymentResponse response = new PaymentResponse();
        response.setId(txn.getId());
        response.setOrderId(txn.getOrderId());
        response.setProvider(txn.getProvider());
        response.setTransactionRef(txn.getTransactionRef());
        response.setStatus(txn.getStatus());
        response.setCreatedAt(txn.getCreatedAt());
        return response;
    }

    // ---- VNPay Helper Methods ----

    private String buildVNPayUrl(String txnRef, BigDecimal amount, String orderInfo, String ipAddress) {
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        String rawData = buildRawData(vnpParams);
        String secureHash = hmacSHA512(hashSecret, rawData);
        return vnpayUrl + "?" + rawData + "&vnp_SecureHash=" + secureHash;
    }

    private String buildRawData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot compute HMAC-SHA512", e);
        }
    }
}
