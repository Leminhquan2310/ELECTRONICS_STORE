package com.electronics_store.dto.product;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionValueDto {
    private Long id;
    private String value;
}