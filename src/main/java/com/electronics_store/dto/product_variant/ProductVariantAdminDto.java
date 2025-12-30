package com.electronics_store.dto.product_variant;

import com.electronics_store.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantAdminDto {
    private Long id;
    private String sku;
    private BigDecimal priceAdjustment;
    private Integer stockQuantity;
    private Product product;
    // Chuỗi: "Size: M, Color: Black"
    private String optionText;
}