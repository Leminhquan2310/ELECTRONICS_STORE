package com.electronics_store.service;

import com.electronics_store.dto.product.ProductDtoCreate;
import com.electronics_store.dto.product.ProductDtoUpdate;
import com.electronics_store.model.ProductImage;
import com.electronics_store.model.ProductOptionValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService<T> {
    boolean create(ProductDtoCreate productDtoCreate);

    T getById(Long id);

    List<T> getAll();

    boolean update(ProductDtoUpdate productDtoUpdate);

    boolean delete(Long id);

    List<ProductImage> getImagesByProductId (Long id);

    List<ProductOptionValue> getProductOptionValuesByProductId(Long id);

    Page<T> getProductBySoldDesc(Pageable pageable);

    Page<T> getProductByCreatedAt(Pageable pageable);

    Page<T> getProductByCategoryName(String categoryName, Pageable pageable);

}
