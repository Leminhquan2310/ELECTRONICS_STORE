package com.electronics_store.service;

import com.electronics_store.dto.option.FilterOptionDto;
import com.electronics_store.dto.product.ProductClientDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ShopService {
    Page<ProductClientDto> filterProducts(String keyword, List<Long> categoryIds, List<Long> optionValueIds, int page, int size, String sortBy);

    List<FilterOptionDto> getFilterOptions();
}
