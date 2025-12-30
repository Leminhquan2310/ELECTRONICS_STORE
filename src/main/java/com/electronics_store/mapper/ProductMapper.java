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

    public Product dtoCreateToEntity(ProductDtoCreate dto, Category category) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setCategory(category);
        return product;
    }

    public Product dtoUpdateToEntity(ProductDtoUpdate dto, Category category) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setCategory(category);
        return product;
    }

    public ProductOption toOptionEntity(Product product, ProductOptionDtoCreate optionDto) {
        ProductOption option = new ProductOption();
        option.setProduct(product);
        option.setName(optionDto.getName());
        return option;
    }

    public ProductOptionValue toOptionValueEntity(ProductOption option, String value) {
        ProductOptionValue optionValue = new ProductOptionValue();
        optionValue.setProductOption(option);
        optionValue.setValue(value);
        return optionValue;
    }

    public ProductDtoUpdate toDtoUpdate(Product product) {
        ProductDtoUpdate productDtoUpdate = new ProductDtoUpdate();
        productDtoUpdate.setId(product.getId());
        productDtoUpdate.setName(product.getName());
        productDtoUpdate.setDescription(product.getDescription());
        productDtoUpdate.setBasePrice(product.getBasePrice());
        productDtoUpdate.setCategoryId(product.getCategory().getId());
        return productDtoUpdate;
    }

    public List<ProductOptionDtoUpdate> toMapOptionValue(List<ProductOptionValue> productOptionValues) {
        Map<Long, ProductOptionDtoUpdate> optionMap = new LinkedHashMap<>();

        for (ProductOptionValue pov : productOptionValues) {

            ProductOption option = pov.getProductOption();
            Long optionId = option.getId();

            // get or create option dto
            ProductOptionDtoUpdate optionDto =
                    optionMap.computeIfAbsent(optionId, id ->
                            new ProductOptionDtoUpdate(
                                    option.getId(),
                                    option.getName(),
                                    new ArrayList<>()
                            )
                    );

            // add value dto
            optionDto.getValues().add(
                    new ProductOptionValueDto(
                            pov.getId(),
                            pov.getValue()
                    )
            );
        }

        return new ArrayList<>(optionMap.values());
    }

    public List<ProductClientDto> toClientDtoList(List<Product> products) {
        return products.stream()
                .map(this::toClientDto)
                .collect(Collectors.toList());
    }

    public List<ProductOptionClientDto> toProductOptionClientDtoList(List<ProductOption> products) {
        return products.stream()
                .map(this::toProductOptionDtoClient)
                .collect(Collectors.toList());
    }

    public ProductClientDto toClientDto(Product product) {
        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        return ProductClientDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .salePrice(product.getBasePrice())
                .images(imageUrls)
                .imageMain(imageUrls.get(0))
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .categoryName(
                        product.getCategory() != null
                                ? product.getCategory().getName()
                                : null
                )
                .build();
    }

    public ProductOptionClientDto toProductOptionDtoClient(ProductOption productOption) {
        return ProductOptionClientDto.builder()
                .id(productOption.getId())
                .name(productOption.getName())
                .values(productOption.getValues().stream().map(this::toProductOptionValueClientDto).toList())
                .build();
    }

    public ProductOptionValueDto toProductOptionValueClientDto(ProductOptionValue productOptionValue) {
        return ProductOptionValueDto.builder()
                .id(productOptionValue.getId())
                .value(productOptionValue.getValue())
                .build();
    }
}
