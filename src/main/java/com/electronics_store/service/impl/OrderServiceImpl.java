package com.electronics_store.service.impl;

import com.electronics_store.dto.order.OrderItemResponse;
import com.electronics_store.dto.order.OrderResponse;
import com.electronics_store.dto.order.OrderStatusHistoryDto;
import com.electronics_store.model.Order;
import com.electronics_store.model.OrderStatusHistory;
import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import com.electronics_store.repository.OrderRepository;
import com.electronics_store.repository.OrderStatusHistoryRepository;
import com.electronics_store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status, PaymentStatus paymentStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        if (paymentStatus != null) {
            order.setPaymentStatus(paymentStatus);
        }
        orderRepository.save(order);
        // Automatically create and save a history record for the status change
        OrderStatusHistory historyRecord = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .changeTime(LocalDateTime.now())
                .description(String.format("Order status changed to %s.", status))
                .build();
        orderStatusHistoryRepository.save(historyRecord);

    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(userId);
        return orders.stream().map(this::mapOrderToOrderResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderStatusHistoryDto> getOrderTrackingHistory(Long orderId) {
        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrder_IdOrderByChangeTimeAsc(orderId);
        return history.stream()
                .map(record -> OrderStatusHistoryDto.builder()
                        .status(record.getStatus())
                        .changeTime(record.getChangeTime())
                        .description(record.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private OrderResponse mapOrderToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .items(order.getOrderItems().stream().map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .variantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                        .variantName(item.getProductVariant() != null ? item.getProductVariant().getSku() : null)
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }

}
