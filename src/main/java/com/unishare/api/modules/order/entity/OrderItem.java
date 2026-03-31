package com.unishare.api.modules.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "item_type", length = 50)
    private String itemType; // document / booking

    @Column(name = "item_id")
    private Long itemId;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
