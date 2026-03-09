package com.electronics_store.dto.order;

import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;

    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;

    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private LocalDateTime orderDate;

    private List<OrderItemResponse> items;
}

