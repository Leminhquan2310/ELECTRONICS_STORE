package com.electronics_store.service.impl;

import com.electronics_store.dto.product_variant.ProductVariantAdminDto;
import com.electronics_store.dto.product_variant.ProductVariantDtoUpdate;
import com.electronics_store.helper.VariantGenerator;
import com.electronics_store.model.*;
import com.electronics_store.repository.*;
import com.electronics_store.service.ProductVariantService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductVariantServiceImpl implements ProductVariantService {
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;


    @Transactional
    @Override
    public void generateVariants(Product product) {
        // 1. Lấy danh sách các giá trị thuộc tính đã chọn cho sản phẩm (từ Select Multiple)
        List<ProductOption> selectedProductOptions = productOptionRepository.findByProductId(product.getId());

        if (selectedProductOptions.isEmpty()) {
            return;
        }

        // 2. Nhóm các OptionValue theo từng loại Option (Màu sắc, Kích thước,...)
        // Map<Long, List<OptionValue>>: { 1(Màu) -> [Đỏ, Xanh], 2(Size) -> [S, M] }
        Map<Long, List<OptionValue>> groupedOptions = selectedProductOptions.stream()
                .filter(po -> po.getOption() != null && po.getOptionValue() != null)
                .collect(Collectors.groupingBy(
                        po -> po.getOption().getId(),
                        Collectors.mapping(ProductOption::getOptionValue, Collectors.toList())
                ));

        // 3. Chuẩn bị danh sách các nhóm giá trị để nhân tổ hợp
        List<List<OptionValue>> listsToCombine = new ArrayList<>(groupedOptions.values());

        // 4. Sinh tổ hợp Cartesian Product (Ví dụ: [Đỏ, S], [Đỏ, M]...)
        List<List<OptionValue>> combinations = VariantGenerator.generate(listsToCombine);

        // 5. Tạo các ProductVariant từ các tổ hợp sinh ra
        List<ProductVariant> newVariants = new ArrayList<>();

        for (List<OptionValue> combo : combinations) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPriceAdjustment(product.getBasePrice()); // Mặc định lấy giá cơ bản
            variant.setStockQuantity(0);
            variant.setActive(true);

            // Gán OptionValues vào Variant thông qua bảng trung gian ProductVariantOptionValue
            // Hàm addOptionValue của bạn sẽ tự tạo đối tượng ProductVariantOptionValue nội bộ
            combo.forEach(variant::addOptionValue);

            // Sinh SKU: VD: "AO-THUN-DO-S"
            variant.setSku(generateSku(product.getName(), combo));

            newVariants.add(variant);
        }

        // 6. Lưu tất cả (Batch Insert)
        productVariantRepository.saveAll(newVariants);
    }

    private String generateSku(String productName, List<OptionValue> combo) {
        // Làm sạch tên sản phẩm: "Áo Thun" -> "AO-THUN"
        String prefix = productName.replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9-]", "").toUpperCase();

        // Lấy các giá trị: "Đỏ", "XL" -> "DO-XL"
        String suffix = combo.stream()
                .map(v -> v.getValue().replaceAll("\\s+", "").toUpperCase())
                .collect(Collectors.joining("-"));

        return prefix + "-" + suffix + "-" + System.currentTimeMillis() % 1000; // Thêm hậu tố tránh trùng
    }

    @Override
    public List<ProductVariantAdminDto> getAll() {
        List<ProductVariant> results = productVariantRepository.findAllByProductIsActiveIsTrue();
        return mapToListProductVariantDto(results);
    }

    public List<ProductVariantAdminDto> mapToListProductVariantDto(List<ProductVariant> productVariants) {
        List<ProductVariantAdminDto> result = new ArrayList<>();
        for (ProductVariant v : productVariants) {
            ProductVariantAdminDto dto = new ProductVariantAdminDto();
            dto.setId(v.getId());
            dto.setSku(v.getSku());
            dto.setPriceAdjustment(v.getPriceAdjustment());
            dto.setStockQuantity(v.getStockQuantity());
            dto.setProduct(v.getProduct());
            String optionText = v.getVariantValues().stream()
                    .map(o -> o.getOptionValue().getOption().getName() + ": " + o.getOptionValue().getValue())
                    .collect(Collectors.joining(", "));

            dto.setOptionText(optionText);
            result.add(dto);
        }
        return result;
    }

    @Override
    public ProductVariant update(Object productVariantDtoUpdate) {
        ProductVariantDtoUpdate productVariantDto = (ProductVariantDtoUpdate) productVariantDtoUpdate;
        ProductVariant productVariant = productVariantRepository.findById(productVariantDto.getId()).get();
        productVariant.setPriceAdjustment(productVariantDto.getPriceAdjustment());
        productVariant.setStockQuantity(productVariantDto.getStockQuantity());
        return productVariantRepository.save(productVariant);
    }

    @Override
    public ProductVariant getByProductIdAndOptionValues(Long productId, List<Long> optionValueIds) {
        return productVariantRepository.findByProductIdAndOptionValueIds(productId, optionValueIds, optionValueIds.size());
    }


}
