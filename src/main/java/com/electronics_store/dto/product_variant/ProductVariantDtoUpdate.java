package com.electronics_store.dto.product_variant;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantDtoUpdate {
    private Long id;

    @Min(value = 0, message = "Stock quantity must be greater than 0")
    private Integer stockQuantity;

    @Min(value = 0, message = "Price adjustment must be greater than 0")
    private BigDecimal priceAdjustment;
}
