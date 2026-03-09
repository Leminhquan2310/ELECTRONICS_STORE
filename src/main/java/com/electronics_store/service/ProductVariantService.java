package com.electronics_store.service;

import com.electronics_store.dto.product_variant.ProductVariantAdminDto;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    void generateVariants(Product product);

    //    String generateSku(Product product, List<ProductVariantValue> variantValues);
    List<ProductVariantAdminDto> getAll();

    ProductVariant update(Object productVariantDtoUpdate);

    ProductVariant getByProductIdAndOptionValues(Long productId, List<Long> optionValues);

}
