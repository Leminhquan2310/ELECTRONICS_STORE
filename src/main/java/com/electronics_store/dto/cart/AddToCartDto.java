package com.electronics_store.dto.cart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddToCartDto {
    private Long productId;

    private List<Long> optionValueIds;

    private int quantity;

}
