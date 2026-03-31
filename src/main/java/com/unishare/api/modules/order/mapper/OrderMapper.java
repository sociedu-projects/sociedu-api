package com.unishare.api.modules.order.mapper;

import com.unishare.api.modules.order.dto.OrderItemResponse;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.entity.Order;
import com.unishare.api.modules.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;
        OrderItemResponse r = new OrderItemResponse();
        r.setId(item.getId());
        r.setItemType(item.getItemType());
        r.setItemId(item.getItemId());
        r.setPrice(item.getPrice());
        return r;
    }

    public OrderResponse toResponse(Order order, List<OrderItem> items) {
        if (order == null) return null;
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setBuyerId(order.getBuyerId());
        r.setType(order.getType());
        r.setStatus(order.getStatus());
        r.setTotalAmount(order.getTotalAmount());
        r.setCreatedAt(order.getCreatedAt());
        if (items != null) {
            r.setItems(items.stream().map(this::toItemResponse).collect(Collectors.toList()));
        }
        return r;
    }
}
