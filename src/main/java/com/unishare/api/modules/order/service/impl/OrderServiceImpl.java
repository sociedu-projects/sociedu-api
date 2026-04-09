package com.unishare.api.modules.order.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.order.dto.OrderRequest;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.entity.Order;
import com.unishare.api.modules.order.entity.OrderItem;
import com.unishare.api.modules.order.exception.OrderErrorCode;
import com.unishare.api.modules.order.mapper.OrderMapper;
import com.unishare.api.modules.order.repository.OrderItemRepository;
import com.unishare.api.modules.order.repository.OrderRepository;
import com.unishare.api.modules.order.service.OrderService;
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
}

