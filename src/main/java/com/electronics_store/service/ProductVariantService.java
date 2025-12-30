package com.electronics_store.service;

import com.electronics_store.model.Product;
import com.electronics_store.model.ProductVariant;
import com.electronics_store.model.ProductVariantValue;

import java.util.List;

public interface ProductVariantService extends IGenerateService<ProductVariant> {
    void generateVariants(Product product);

    String generateSku(Product product, List<ProductVariantValue> variantValues);

    void syncVariants(Product product);
}
