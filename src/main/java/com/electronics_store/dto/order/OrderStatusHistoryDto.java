package com.electronics_store.dto.order;

import com.electronics_store.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OrderStatusHistoryDto {
    private OrderStatus status;
    private LocalDateTime changeTime;
    private String description;
}
