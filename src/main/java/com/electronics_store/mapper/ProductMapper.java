package com.electronics_store.mapper;

import com.electronics_store.dto.product.*;
import com.electronics_store.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductMapper {


    public Product dtoUpdateToEntity(ProductDtoUpdate dto, Category category) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setCategory(category);
        return product;
    }


    public OptionValue toOptionValueEntity(ProductOption option, String value) {
        OptionValue optionValue = new OptionValue();
//        optionValue.setProductOption(option);
        optionValue.setValue(value);
        return optionValue;
    }


//    public List<ProductOptionDtoUpdate> toMapOptionValue(List<OptionValue> optionValues) {
//        Map<Long, ProductOptionDtoUpdate> optionMap = new LinkedHashMap<>();
//
//        for (OptionValue pov : optionValues) {
//
//            ProductOption option = pov.getProductOption();
//            Long optionId = option.getId();
//
//            // get or create option dto
//            ProductOptionDtoUpdate optionDto =
//                    optionMap.computeIfAbsent(optionId, id ->
//                            new ProductOptionDtoUpdate(
//                                    option.getId(),
//                                    option.getName(),
//                                    new ArrayList<>()
//                            )
//                    );
//
//            // add value dto
//            optionDto.getValues().add(
//                    new ProductOptionValueDto(
//                            pov.getId(),
//                            pov.getValue()
//                    )
//            );
//        }
//
//        return new ArrayList<>(optionMap.values());
//    }

    public List<ProductClientDto> toClientDtoList(List<Product> products) {
        return products.stream()
                .map(this::toClientDto)
                .collect(Collectors.toList());
    }

    private  ProductClientDto toClientDto(Product product) {
        return new ProductClientDto();
    }

    public List<ProductOptionClientDto> toProductOptionClientDtoList(List<ProductOption> products) {
        return products.stream()
                .map(this::toProductOptionDtoClient)
                .collect(Collectors.toList());
    }


    public ProductOptionClientDto toProductOptionDtoClient(ProductOption productOption) {
        return ProductOptionClientDto.builder()
                .id(productOption.getId())
//                .name(productOption.getName())
//                .values(productOption.getValues().stream().map(this::toProductOptionValueClientDto).toList())
                .build();
    }

    public ProductOptionValueDto toProductOptionValueClientDto(OptionValue optionValue) {
        return ProductOptionValueDto.builder()
                .id(optionValue.getId())
                .value(optionValue.getValue())
                .build();
    }
}
