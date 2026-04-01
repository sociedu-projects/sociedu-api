package com.unishare.api.modules.order.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.products.service.DocumentService;
import com.unishare.api.modules.order.dto.OrderRequest;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.entity.Order;
import com.unishare.api.modules.order.entity.OrderItem;
import com.unishare.api.modules.order.exception.OrderErrorCode;
import com.unishare.api.modules.order.mapper.OrderMapper;
import com.unishare.api.modules.order.repository.OrderItemRepository;
import com.unishare.api.modules.order.repository.OrderRepository;
import com.unishare.api.modules.order.service.OrderService;
import com.unishare.api.modules.payment.dto.PaymentResponse;
import com.unishare.api.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final DocumentService documentService;

    @Override
    @Transactional
    public OrderResponse createDocumentOrder(Long buyerId, OrderRequest request) {
        Long documentId = request.getDocumentId();

        // 1. Kiểm tra document tồn tại và đang published
        if (!documentService.isDocumentPublished(documentId)) {
            throw new AppException(OrderErrorCode.DOCUMENT_NOT_AVAILABLE);
        }

        // 2. Không cho mua lại
        if (orderRepository.hasBuyerPurchasedDocument(buyerId, documentId)) {
            throw new AppException(OrderErrorCode.ALREADY_PURCHASED);
        }

        // 3. Lấy giá và tên tài liệu
        java.math.BigDecimal docPrice = documentService.getDocumentPrice(documentId);
        String docTitle = documentService.getDocumentTitle(documentId);

        // 4. Tạo Order
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setType("document");
        order.setStatus("pending");
        order.setTotalAmount(docPrice);
        order = orderRepository.save(order);

        // 5. Tạo OrderItem
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setItemType("document");
        item.setItemId(documentId);
        item.setPrice(docPrice);
        orderItemRepository.save(item);

        // 6. Gọi PaymentService để tạo VNPay payment URL
        String orderInfo = "Mua tai lieu: " + docTitle;
        String clientIp = request.getClientIp() != null ? request.getClientIp() : "127.0.0.1";
        PaymentResponse payment = paymentService.createPayment(order.getId(), docPrice, orderInfo, clientIp);

        log.info("Created document order id={} for buyer={}, document={}", order.getId(), buyerId, documentId);

        // 7. Build response
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        OrderResponse response = orderMapper.toResponse(order, items);
        response.setPaymentUrl(payment.getPaymentUrl());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return orderMapper.toResponse(order, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getBuyerId().equals(buyerId))
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return orderMapper.toResponse(order, items);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedDocument(Long buyerId, Long documentId) {
        return orderRepository.hasBuyerPurchasedDocument(buyerId, documentId);
    }
}
