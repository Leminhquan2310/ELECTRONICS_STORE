package com.electronics_store.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
public class CartSummaryDto {
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal subTotal = BigDecimal.ZERO;
}

