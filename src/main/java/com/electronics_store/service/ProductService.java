package com.electronics_store.service;

import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.dto.product.ProductDtoCreate;
import com.electronics_store.dto.product.ProductDtoUpdate;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductImage;
import com.electronics_store.model.OptionValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    boolean create(ProductDtoCreate productDtoCreate);

    ProductDtoUpdate getProductForUpdate(Long id);

    ProductClientDto getProductForClient(Long id);

    List<Product> getAll();

    boolean update(ProductDtoUpdate productDtoUpdate);

    boolean delete(Long id);

    Page<ProductClientDto> searchForClient(String keyword,List<String> optionValues, int page, int size);

    List<ProductImage> getImagesByProductId (Long id);

    List<OptionValue> getProductOptionValuesByProductId(Long id);

    Page<ProductClientDto> getProductBySoldDesc(Pageable pageable);

     Page<ProductClientDto> getProductByCreatedAt(int page, int size);

    Page<ProductClientDto> getProductByCategoryName(String categoryName, Pageable pageable);

}
