package com.electronics_store.service.impl;

import com.electronics_store.dto.product_variant.ProductVariantAdminDto;
import com.electronics_store.dto.product_variant.ProductVariantDtoUpdate;
import com.electronics_store.helper.VariantGenerator;
import com.electronics_store.model.*;
import com.electronics_store.repository.OptionRepository;
import com.electronics_store.repository.OptionValueRepository;
import com.electronics_store.repository.ProductOptionRepository;
import com.electronics_store.repository.ProductVariantRepository;
import com.electronics_store.service.ProductVariantService;
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
    private OptionRepository optionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;
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

    @Transactional
    public void syncVariants(Product product) {
        // 1. Sinh ra các tổ hợp mong muốn hiện tại (từ ProductOption mới update)
        List<List<OptionValue>> combinations = generateCombinationsFromProduct(product);

        // 2. Lấy danh sách Variant hiện có trong DB
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());

        // 3. Tạo Map SKU hoặc Map tập hợp OptionValue ID để so sánh
        Map<String, ProductVariant> existingMap = existingVariants.stream()
                .collect(Collectors.toMap(this::getVariantKey, v -> v));

        List<ProductVariant> toSave = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        for (List<OptionValue> combo : combinations) {
            String key = getComboKey(combo);
            processedKeys.add(key);

            if (existingMap.containsKey(key)) {
                // Nếu đã tồn tại tổ hợp này: Active lại nếu nó đang bị disable
                ProductVariant v = existingMap.get(key);
                v.setActive(true);
                toSave.add(v);
            } else {
                // Nếu là tổ hợp mới hoàn toàn: Thêm mới
                ProductVariant newV = new ProductVariant();
                newV.setProduct(product);
                newV.setPriceAdjustment(product.getBasePrice());
                newV.setStockQuantity(0);
                newV.setSku(generateSku(product.getName(), combo));
                combo.forEach(newV::addOptionValue);
                toSave.add(newV);
            }
        }

        // 4. Những Variant cũ không còn nằm trong tổ hợp mới -> Deactivate thay vì Delete
        for (ProductVariant v : existingVariants) {
            if (!processedKeys.contains(getVariantKey(v))) {
                if (canDeleteVariant(v)) {
                    productVariantRepository.delete(v);
                } else {
                    v.setActive(false); // Giữ lại để bảo toàn lịch sử đơn hàng
                }
            }
        }

        productVariantRepository.saveAll(toSave);
    }

    // Hàm check xem Variant này đã có đơn hàng chưa
    private boolean canDeleteVariant(ProductVariant v) {
        // return !orderItemRepository.existsByVariantId(v.getId());
        return false; // Mặc định trả về false để an toàn (luôn deactivate)
    }

    // Tạo key đại diện cho tổ hợp: ví dụ "10-25" (optionValueId1-optionValueId2)
    private String getVariantKey(ProductVariant v) {
        return v.getVariantValues().stream()
                .map(vv -> vv.getOptionValue().getId().toString())
                .sorted()
                .collect(Collectors.joining("-"));
    }

    private String getComboKey(List<OptionValue> combo) {
        return combo.stream()
                .map(ov -> ov.getId().toString())
                .sorted()
                .collect(Collectors.joining("-"));
    }

    private List<List<OptionValue>> generateCombinationsFromProduct(Product product) {
        // 1. Lấy tất cả các ProductOption hiện có của Product
        // Giả sử product.getProductOptions() đã được fetch hoặc lấy từ DB
        List<ProductOption> productOptions = product.getProductOptions();

        if (productOptions == null || productOptions.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Nhóm các OptionValue theo Option ID
        // Kết quả: Map<OptionID, List<OptionValue>>
        // Ví dụ: { 1(Màu) -> [Đỏ, Xanh], 2(Size) -> [S, M] }
        Map<Long, List<OptionValue>> groupedOptions = productOptions.stream()
                .filter(po -> po.getOption() != null && po.getOptionValue() != null)
                .collect(Collectors.groupingBy(
                        po -> po.getOption().getId(),
                        Collectors.mapping(ProductOption::getOptionValue, Collectors.toList())
                ));

        // 3. Chuyển Map thành List các List để đưa vào thuật toán tổ hợp
        List<List<OptionValue>> listsToCombine = new ArrayList<>(groupedOptions.values());

        // 4. Gọi thuật toán nhân tổ hợp (Cartesian Product)
        return VariantGenerator.generate(listsToCombine);
    }
    @Override
    public List<ProductVariantAdminDto> getAll() {
        List<ProductVariant> results =  productVariantRepository.findAll();
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


}
