package com.electronics_store.service;

import com.electronics_store.dto.option.OptionDtoWithValues;

import java.util.List;

public interface ProductOptionService {
    List<OptionDtoWithValues> getOptionsByProductId(Long productId);
}
