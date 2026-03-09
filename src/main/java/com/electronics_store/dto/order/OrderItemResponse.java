package com.electronics_store.dto.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long productId;
    private String productName;

    private Long variantId;
    private String variantName;

    private Integer quantity;
    private BigDecimal price;
}
