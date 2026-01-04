package com.electronics_store.service;


import com.electronics_store.dto.option.OptionDto;
import com.electronics_store.dto.option.OptionDtoWithValues;

import java.util.List;

public interface OptionService {
    void create(String name);

    List<OptionDto> findAll();

    List<OptionDtoWithValues> findAllOptionDtoHaveChildrenList();


    void updateOption(Long id, String newName);

    boolean isDuplicate(String name);

    Long getUsageCount(Long id);
}
