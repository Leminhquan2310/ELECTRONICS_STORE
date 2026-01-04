package com.electronics_store.service.impl;

import com.electronics_store.dto.option_value.OptionValueDto;
import com.electronics_store.model.Option;
import com.electronics_store.model.OptionValue;
import com.electronics_store.repository.OptionRepository;
import com.electronics_store.repository.OptionValueRepository;
import com.electronics_store.service.OptionValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptionValueServiceImpl implements OptionValueService {
    @Autowired
    private OptionValueRepository optionValueRepository;
    @Autowired
    private OptionRepository optionRepository;

    @Override
    public OptionValueDto create(Long optionId, String value) {
        Option option = optionRepository.findById(optionId).get();
        OptionValue optionValue = new OptionValue();
        optionValue.setValue(value);
        optionValue.setOption(option);
        return convertToDto(optionValueRepository.save(optionValue));
    }

    @Override
    public List<OptionValueDto> getValuesByOptionId(Long optionId) {
        List<OptionValue> values = optionValueRepository.findAllByOptionId(optionId);

        // Convert Entity sang DTO
        return values.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public void updateValueName(Long id, String value) {
        OptionValue optionValue = optionValueRepository.findById(id).get();
        optionValue.setValue(value);
        optionValueRepository.save(optionValue);
    }

    private OptionValueDto convertToDto(OptionValue optionValue) {
        return new OptionValueDto(optionValue.getId(), optionValue.getValue());
    }
}
