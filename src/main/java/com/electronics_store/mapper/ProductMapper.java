package com.electronics_store.mapper;

import com.electronics_store.dto.product.*;
import com.electronics_store.model.Category;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductOption;
import com.electronics_store.model.ProductOptionValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    new ProductOptionValueDtoUpdate(
                            pov.getId(),
                            pov.getValue()
                    )
            );
        }

        return new ArrayList<>(optionMap.values());
    }
}
