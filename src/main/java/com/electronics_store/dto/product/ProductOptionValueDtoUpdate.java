package com.electronics_store.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductOptionValueDtoUpdate {
    private Long id;
    private String value;
}
