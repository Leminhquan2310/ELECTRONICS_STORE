package com.electronics_store.service.impl;

import com.electronics_store.dto.option.OptionDto;
import com.electronics_store.dto.option.OptionDtoWithValues;
import com.electronics_store.dto.option_value.OptionValueDto;
import com.electronics_store.model.Option;
import com.electronics_store.repository.OptionRepository;
import com.electronics_store.repository.OptionValueRepository;
import com.electronics_store.repository.ProductOptionRepository;
import com.electronics_store.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OptionServiceImpl implements OptionService {
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;



    @Override
    public void create(String name) {
        // 1. Lưu Option trước
        Option option = new Option();
        option.setName(name);
        optionRepository.save(option);
    }

    @Override
    public List<OptionDto> findAll() {
        List<Option> options = optionRepository.findAll();
        return options.stream().map(option -> new OptionDto(option.getId(), option.getName())).toList();
    }

    @Override
    public List<OptionDtoWithValues> findAllOptionDtoHaveChildrenList() {
        List<Option> options = optionRepository.findAll();
        return options.stream().map(this::mapToDto).toList();
    }


    private OptionDtoWithValues mapToDto(Option option) {
        return OptionDtoWithValues.builder()
                .id(option.getId())
                .name(option.getName())
                .values(option.getValues().stream().map(ov -> new OptionValueDto(ov.getId(), ov.getValue())).toList())
                .build();
    }

    @Transactional
    public void updateOption(Long id, String newName) {
        Option option = optionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Option not found"));

        // Cập nhật tên
        option.setName(newName);
        optionRepository.save(option);
    }

    @Override
    public boolean isDuplicate(String name) {
        return optionRepository.existsByName(name);
    }

    public Long getUsageCount(Long id) {
        return productOptionRepository.countProductsUsingOption(id);
    }
}
