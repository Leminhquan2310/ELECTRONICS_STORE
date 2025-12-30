package com.electronics_store.mapper;

import com.electronics_store.dto.product_variant.ProductVariantAdminDto;
import com.electronics_store.model.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductVariantMapper {

    public List<ProductVariantAdminDto> listProductVariantToAdminDto(List<ProductVariant> productVariants) {
        List<ProductVariantAdminDto> result = new ArrayList<>();
        for (ProductVariant v : productVariants) {
            ProductVariantAdminDto dto = new ProductVariantAdminDto();
            dto.setId(v.getId());
            dto.setSku(v.getSku());
            dto.setPriceAdjustment(v.getPriceAdjustment());
            dto.setStockQuantity(v.getStock_quantity());
            dto.setProduct(v.getProduct());
            String optionText = v.getVariantValues().stream()
                    .map(o -> o.getOptionValue().getProductOption().getName() + ": " + o.getOptionValue().getValue())
                    .collect(Collectors.joining(", "));

            dto.setOptionText(optionText);
            result.add(dto);
        }
        return result;
    }
}
