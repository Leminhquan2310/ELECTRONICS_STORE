package com.electronics_store.service.impl;

import com.electronics_store.dto.option.OptionDtoWithValues;
import com.electronics_store.dto.option_value.OptionValueDto;
import com.electronics_store.model.Option;
import com.electronics_store.model.OptionValue;
import com.electronics_store.model.ProductOption;
import com.electronics_store.repository.ProductOptionRepository;
import com.electronics_store.service.ProductOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductOptionServiceImpl implements ProductOptionService {
    @Autowired
    private ProductOptionRepository productOptionRepository;

    public List<OptionDtoWithValues> getOptionsByProductId(Long productId) {
        // 1. Lấy toàn bộ các dòng cấu hình phẳng từ DB
        List<ProductOption> productOptions = productOptionRepository.findAllByProductIdWithDetails(productId);

        // 2. Nhóm dữ liệu theo Option
        // Map<Option, List<OptionValue>>
        Map<Option, List<OptionValue>> grouped = productOptions.stream()
                .collect(Collectors.groupingBy(
                        ProductOption::getOption,
                        Collectors.mapping(ProductOption::getOptionValue, Collectors.toList())
                ));

        // 3. Chuyển đổi Map thành List DTO để trả về
        return grouped.entrySet().stream()
                .map(entry -> {
                    Option opt = entry.getKey();
                    List<OptionValueDto> valueDtos = entry.getValue().stream()
                            .map(v -> new OptionValueDto(v.getId(), v.getValue()))
                            .collect(Collectors.toList());

                    return new OptionDtoWithValues(opt.getId(), opt.getName(), valueDtos);
                })
                .collect(Collectors.toList());
    }
}
