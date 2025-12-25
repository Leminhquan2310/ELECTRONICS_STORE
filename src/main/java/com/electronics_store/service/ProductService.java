package com.electronics_store.service;

import com.electronics_store.dto.product.ProductDtoCreate;
import com.electronics_store.dto.product.ProductDtoUpdate;
import com.electronics_store.model.ProductImage;
import com.electronics_store.model.ProductOptionValue;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;

public interface ProductService<T> {
    boolean create(ProductDtoCreate productDtoCreate);
//
//    List<T> createBatch(List<T> ts);
//
    T getById(Long id);

    List<T> getAll();

    boolean update(ProductDtoUpdate productDtoUpdate);

    boolean delete(Long id);

    List<ProductImage> getImagesByProductId (Long id);

    List<ProductOptionValue> getProductOptionValuesByProductId(Long id);
}
