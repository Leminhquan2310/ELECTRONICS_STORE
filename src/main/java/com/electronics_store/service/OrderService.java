package com.electronics_store.service;

import com.electronics_store.dto.order.OrderResponse;
import com.electronics_store.dto.order.OrderStatusHistoryDto;
import com.electronics_store.model.Order;
import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;

import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();

    Order getOrderById(Long id);

    void updateStatus(Long orderId, OrderStatus status, PaymentStatus paymentStatus);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderStatusHistoryDto> getOrderTrackingHistory(Long orderId);
}