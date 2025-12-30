package com.electronics_store.dto.product;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductOptionClientDto {
    private Long id;
    private String name;
    private List<ProductOptionValueDto> values;
}