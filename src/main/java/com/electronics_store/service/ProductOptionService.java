package com.electronics_store.service;

import com.electronics_store.model.ProductOption;

import java.util.List;

public interface ProductOptionService extends IGenerateService<ProductOption> {
    List<ProductOption> getProductOptionsByProductId(Long productId);
}
