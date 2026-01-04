package com.electronics_store.dto.product;

import lombok.*;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductOptionCartDto {
    private Long productOptionId;
    private List<Long> values;
}
