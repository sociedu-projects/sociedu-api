package com.unishare.api.modules.order.mapper;

import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setBuyerId(order.getBuyerId());
        r.setServiceId(order.getServiceId());
        r.setStatus(order.getStatus());
        r.setTotalAmount(order.getTotalAmount());
        r.setPaidAt(order.getPaidAt());
        r.setCreatedAt(order.getCreatedAt());
        return r;
    }
}
