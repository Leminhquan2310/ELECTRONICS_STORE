package com.electronics_store.service.impl;

import com.electronics_store.dto.product.ProductDtoCreate;
import com.electronics_store.dto.product.ProductDtoUpdate;
import com.electronics_store.dto.product_variant.ProductVariantDtoUpdate;
import com.electronics_store.helper.VariantGenerator;
import com.electronics_store.model.*;
import com.electronics_store.repository.ProductOptionRepository;
import com.electronics_store.repository.ProductOptionValueRepository;
import com.electronics_store.repository.ProductVariantRepository;
import com.electronics_store.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private ProductOptionValueRepository productOptionValueRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Transactional
    public void generateVariants(Product product) {

        // 1. Lấy option
        List<ProductOption> options =
                productOptionRepository.findByProductId(product.getId());

        if (options.isEmpty()) return;

        // 2. Lấy value theo từng option
        List<List<ProductOptionValue>> groups = options.stream()
                .map(productOptionValueRepository::findByProductOption)
                .filter(list -> !list.isEmpty())
                .toList();

        if (groups.isEmpty()) return;

        // 3. Sinh tổ hợp option value
        List<List<ProductOptionValue>> combinations =
                VariantGenerator.generate(groups);

        // 4. Tạo variant
        for (List<ProductOptionValue> values : combinations) {

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPriceAdjustment(product.getBasePrice());
            variant.setStock_quantity(0);

            // map option value → variant value
            values.forEach(variant::addOptionValue);

            // sinh SKU SAU khi add value
            variant.setSku(generateSku(product, variant.getVariantValues()));

            productVariantRepository.save(variant);
        }
    }

    public String generateSku(Product product, List<ProductVariantValue> variantValues) {

        String optionPart = variantValues.stream()
                .sorted(Comparator.comparing(v ->
                        v.getOptionValue()
                                .getProductOption()
                                .getId()
                ))
                .map(v -> v.getOptionValue().getValue())
                .collect(Collectors.joining("-"));

        return ("P" + product.getId() + "-" + optionPart).toUpperCase();
    }

    @Transactional
    public void syncVariants(Product product) {

        // 1. Lấy option + value
        List<ProductOption> options = productOptionRepository.findByProductId(product.getId());
        if (options.isEmpty()) return;

        List<List<ProductOptionValue>> groups = options.stream()
                .map(productOptionValueRepository::findByProductOption)
                .filter(list -> !list.isEmpty())
                .toList();

        if (groups.isEmpty()) return;

        // 2. Sinh tổ hợp
        List<List<ProductOptionValue>> combinations = VariantGenerator.generate(groups);

        // 3. Lấy variant hiện tại
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());

        Map<Set<Long>, ProductVariant> existingMap = new HashMap<>();
        for (ProductVariant variant : existingVariants) {
            Set<Long> valueIds = variant.getVariantValues().stream()
                    .map(v -> v.getOptionValue().getId())
                    .collect(Collectors.toSet());
            existingMap.put(valueIds, variant);
        }

        // 4. Xử lý từng tổ hợp
        for (List<ProductOptionValue> values : combinations) {
            Set<Long> ids = values.stream().map(ProductOptionValue::getId).collect(Collectors.toSet());

            if (!existingMap.containsKey(ids)) {
                // Tạo mới
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setPriceAdjustment(product.getBasePrice());
                variant.setStock_quantity(0);
                values.forEach(variant::addOptionValue);
                variant.setSku(generateSku(product, variant.getVariantValues()));
                productVariantRepository.save(variant);
            } else {
                // Giữ stock cũ, loại khỏi danh sách xoá
                existingMap.remove(ids);
            }
        }

        // 5. Xoá variant cũ không còn hợp lệ
        existingMap.values().forEach(productVariantRepository::delete);
    }

    @Override
    public ProductVariant create(Object t) throws ChangeSetPersister.NotFoundException {
        return null;
    }

    @Override
    public List<ProductVariant> createBatch(List<ProductVariant> productVariants) {
        return List.of();
    }

    @Override
    public ProductVariant getById(Long id) {
        return null;
    }

    @Override
    public List<ProductVariant> getAll() {
        return productVariantRepository.findAll();
    }

    @Override
    public ProductVariant update(Object productVariantDtoUpdate) {
        ProductVariantDtoUpdate productVariantDto = (ProductVariantDtoUpdate) productVariantDtoUpdate;
        ProductVariant productVariant = productVariantRepository.findById(productVariantDto.getId()).get();
        productVariant.setPriceAdjustment(productVariantDto.getPriceAdjustment());
        productVariant.setStock_quantity(productVariantDto.getStockQuantity());
        return productVariantRepository.save(productVariant);
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
