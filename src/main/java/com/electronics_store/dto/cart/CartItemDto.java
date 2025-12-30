package com.electronics_store.dto.cart;

import com.electronics_store.dto.product.ProductClientDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CartItemDto {
    private Long id;
    private ProductClientDto product;
    private String variantSku;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;
}
