package com.electronics_store.service;

import com.electronics_store.dto.option_value.OptionValueDto;

import java.util.List;

public interface OptionValueService {
    OptionValueDto create(Long optionId, String value);

    List<OptionValueDto> getValuesByOptionId(Long optionId);

    void updateValueName(Long id, String value);
}
