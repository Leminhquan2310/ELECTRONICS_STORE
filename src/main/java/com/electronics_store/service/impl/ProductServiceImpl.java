package com.electronics_store.service.impl;

import com.electronics_store.dto.image.ImageUploadResult;
import com.electronics_store.dto.option.OptionDto;
import com.electronics_store.dto.option.OptionInputDto;
import com.electronics_store.dto.product.*;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.*;
import com.electronics_store.repository.*;
import com.electronics_store.service.ImageStorageService;
import com.electronics_store.service.ProductService;
import com.electronics_store.service.ProductVariantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private OptionValueRepository optionValueRepository;
    @Autowired
    private ImageStorageService imageStorageService;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ProductVariantService variantService;

    @Transactional
    @Override
    public boolean create(ProductDtoCreate productDtoCreate) {
        try {
            // 1. Tạo và map dữ liệu cơ bản cho Product
            Category category = categoryRepository.findById(productDtoCreate.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            Product product = mapToProduct(productDtoCreate, category);

            // 2. Xử lý phần Product Options
            if (productDtoCreate.getOptions() != null) {
                for (OptionInputDto input : productDtoCreate.getOptions()) {

                    // Bỏ qua nếu dữ liệu rác (không chọn option hoặc không chọn value nào)
                    if (input.getOptionId() == null || input.getValueIds() == null || input.getValueIds().isEmpty()) {
                        continue;
                    }

                    // Lấy Proxy object của Option (để set FK mà không cần query DB select *)
                    Option option = optionRepository.getReferenceById(input.getOptionId());

                    // Lặp qua từng Value ID người dùng đã chọn (Select multiple)
                    for (Long valId : input.getValueIds()) {
                        OptionValue value = optionValueRepository.getReferenceById(valId);

                        // Tạo entity liên kết
                        ProductOption attribute = new ProductOption();
                        attribute.setProduct(product); // Gán sản phẩm
                        attribute.setOption(option);   // Gán loại option
                        attribute.setOptionValue(value);     // Gán giá trị

                        // Thêm vào list của product
                        product.getProductOptions().add(attribute);
                    }
                }
            }
            productRepository.save(product);

            variantService.generateVariants(product);

            // 2. Lưu từng ảnh
            if (productDtoCreate.getImages() != null && !productDtoCreate.getImages().isEmpty()) {
                for (MultipartFile file : productDtoCreate.getImages()) {

                    if (file.isEmpty()) continue;

                    ImageUploadResult upload = imageStorageService.upload(file);

                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setImageUrl(upload.getUrl());
                    image.setPublicId(upload.getPublicId());

                    productImageRepository.save(image);
                }
            }
            return true;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Create product failed", e);
            return false;
        }
    }

    private Product mapToProduct(ProductDtoCreate productDtoCreate, Category category) {
        Product product = new Product();
        product.setName(productDtoCreate.getName());
        product.setDescription(productDtoCreate.getDescription());
        product.setBasePrice(productDtoCreate.getBasePrice());
        product.setCategory(category);
        return product;
    }

    private Product mapToProduct(Product product, ProductDtoUpdate productDtoUpdate, Category category) {
        product.setName(productDtoUpdate.getName());
        product.setDescription(productDtoUpdate.getDescription());
        product.setBasePrice(productDtoUpdate.getBasePrice());
        product.setCategory(category);
        return product;
    }

    @Override
    public ProductDtoUpdate getProductForUpdate(Long id) {
        Product product = productRepository.findById(id).get();
        return mapToProductDtoUpdate(product);
    }

    @Override
    public ProductClientDto getProductForClient(Long id) {
        Product product = productRepository.findById(id).get();
        return mapToProductClientDto(product);
    }

    private ProductDtoUpdate mapToProductDtoUpdate(Product product) {
        ProductDtoUpdate productDtoUpdate = new ProductDtoUpdate();
        productDtoUpdate.setId(product.getId());
        productDtoUpdate.setName(product.getName());
        productDtoUpdate.setDescription(product.getDescription());
        productDtoUpdate.setBasePrice(product.getBasePrice());
        productDtoUpdate.setCategoryId(product.getCategory().getId());
        return productDtoUpdate;
    }

    public ProductClientDto mapToProductClientDto(Product product) {
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

    @Override
    public List<Product> getAll() {
        return productRepository.findProductsByIsActiveTrue();
    }

    @Transactional
    @Override
    public boolean update(ProductDtoUpdate productDtoUpdate) {
        try {
            Product product = productRepository.findById(productDtoUpdate.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            Category category = categoryRepository.findById(productDtoUpdate.getCategoryId()).orElseThrow();

            // 1. Map thông tin cơ bản
            mapToProduct(product, productDtoUpdate, category);

            // 2. Cập nhật ảnh
            updateImages(product, productDtoUpdate);

            // 3. Đồng bộ Options và OptionValues (Smart Sync)
            syncProductOptions(product, productDtoUpdate.getOptions());

            // 4. Lưu product trước để đảm bảo ID và các quan hệ Option đã ổn định
            productRepository.save(product);

            // 5. Đồng bộ Biến thể (Smart Sync Variants)
            variantService.syncVariants(product);

            return true;
        } catch (Exception e) {
            log.error("Update product failed: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private void syncProductOptions(Product product, List<OptionInputDto> newOptions) {
        // Lấy list hiện tại từ DB
        List<ProductOption> currentOptions = product.getProductOptions();

        // Tạo map để kiểm tra cho nhanh: "optionId-valueId"
        Set<String> newKeys = new HashSet<>();
        for (OptionInputDto input : newOptions) {
            for (Long valId : input.getValueIds()) {
                newKeys.add(input.getOptionId() + "-" + valId);
            }
        }

        // 1. Xóa những OptionValue không còn được chọn
        currentOptions.removeIf(existing -> {
            String key = existing.getOption().getId() + "-" + existing.getOptionValue().getId();
            boolean toRemove = !newKeys.contains(key);
            if (toRemove && isVariantInOrder(existing)) {
                // Nếu giá trị này đã có trong đơn hàng, có thể bạn không muốn xóa
                // mà chỉ nên deactivate nó ở tầng Variant. Ở đây tạm thời cho phép xóa liên kết cấu hình.
            }
            return toRemove;
        });

        // 2. Thêm những OptionValue mới được chọn
        Set<String> currentKeys = currentOptions.stream()
                .map(o -> o.getOption().getId() + "-" + o.getOptionValue().getId())
                .collect(Collectors.toSet());

        for (OptionInputDto input : newOptions) {
            for (Long valId : input.getValueIds()) {
                String key = input.getOptionId() + "-" + valId;
                if (!currentKeys.contains(key)) {
                    ProductOption po = new ProductOption();
                    po.setProduct(product);
                    po.setOption(optionRepository.getReferenceById(input.getOptionId()));
                    po.setOptionValue(optionValueRepository.getReferenceById(valId));
                    currentOptions.add(po);
                }
            }
        }
    }

    // Hàm check xem thuộc tính này có đang nằm trong variant nào đã được đặt hàng không
    private boolean isVariantInOrder(ProductOption option) {
        // Logic này sẽ mở rộng sau khi bạn có bảng OrderItem
        // return orderItemRepository.existsByVariant_VariantValues_OptionValue(option.getOptionValue());
        return false;
    }

//    @Transactional
//    @Override
//    public boolean update(ProductDtoUpdate productDtoUpdate) {
//        try {
//            Category category = categoryRepository.findById(productDtoUpdate.getCategoryId())
//                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//            Product product = productMapper.dtoUpdateToEntity(productDtoUpdate, category);
//            productRepository.save(product);
//
//            updateOptions(product, productDtoUpdate.getOptions());
//
//            variantService.syncVariants(product);
//
//            updateImages(product, productDtoUpdate);
//            return true;
//        } catch (Exception e) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            log.error("Create product failed", e);
//            return false;
//        }
//    }

    @Override
    public boolean delete(Long id) {
        if (checkCanDelete(id)) {
            Product product = productRepository.findById(id).get();
            product.setIsActive(false);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    @Override
    public Page<ProductClientDto> searchForClient(String keyword, List<String> optionValues, int page, int size) {
        Specification<Product> spec = Specification.unrestricted();

        // Lọc sản phẩm đang hoạt động
        spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isActive")));

        // Lọc theo keyword (nếu có)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("category").get("name")), pattern)
            ));
        }

        // XỬ LÝ CHECKED: Lọc theo danh sách String giá trị
        if (optionValues != null && !optionValues.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true); // Tránh lặp sản phẩm khi khớp nhiều variant
                return root.join("variants")
                        .join("optionValues")
                        .get("value") // Lọc theo field 'value' (String) thay vì 'id'
                        .in(optionValues);
            });
        }

        log.info("Search for client: {}", spec);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    private ProductClientDto convertToDto(Product product) {
        // Kiểm tra danh sách ảnh để tránh lỗi crash khi lấy phần tử đầu tiên
        String mainImage = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : "default-image.jpg";

        return ProductClientDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .salePrice(product.getBasePrice()) // Nên kiểm tra logic giảm giá ở đây nếu có
                .imageMain(mainImage)
                .images(product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized")
                .build();
    }

    private boolean checkCanDelete(Long id) {
        // check product in order, invoice,...
        return true;
    }

    private void saveNewOptions(Product product, List<OptionInputDto> options) {
        // 2. Xử lý phần Product Options
        if (options != null) {
            for (OptionInputDto input : options) {

                // Bỏ qua nếu dữ liệu rác (không chọn option hoặc không chọn value nào)
                if (input.getOptionId() == null || input.getValueIds() == null || input.getValueIds().isEmpty()) {
                    continue;
                }

                // Lấy Proxy object của Option (để set FK mà không cần query DB select *)
                Option option = optionRepository.getReferenceById(input.getOptionId());

                // Lặp qua từng Value ID người dùng đã chọn (Select multiple)
                for (Long valId : input.getValueIds()) {
                    OptionValue value = optionValueRepository.getReferenceById(valId);

                    // Tạo entity liên kết
                    ProductOption attribute = new ProductOption();
                    attribute.setProduct(product); // Gán sản phẩm
                    attribute.setOption(option);   // Gán loại option
                    attribute.setOptionValue(value);     // Gán giá trị

                    // Thêm vào list của product
                    product.getProductOptions().add(attribute);
                }
            }
        }
    }

//    private void updateOptionValues(ProductOption option, List<ProductOptionValueDto> valueDtos) {
//        if (valueDtos == null) return;
//
//        List<OptionValue> existingValues = optionValueRepository.findByProductOptionId(option.getId());
//
//        Map<Long, OptionValue> valueMap = existingValues.stream()
//                .collect(Collectors.toMap(OptionValue::getId, v -> v));
//
//        for (ProductOptionValueDto dto : valueDtos) {
//            // Bỏ qua null hoặc rỗng
//            if (dto == null || dto.getValue() == null || dto.getValue().isBlank()) continue;
//
//            OptionValue value = dto.getId() != null
//                    ? valueMap.remove(dto.getId())
//                    : new OptionValue();
//

    /// /            value.setProductOption(option);
//            value.setValue(dto.getValue());
//
//            optionValueRepository.save(value);
//        }
//
//        // DELETE value bị remove
//        valueMap.values().forEach(optionValueRepository::delete);
//    }
    private void updateImages(Product product, ProductDtoUpdate dto) {
        // DELETE ảnh
        if (dto.getImageIdDelete() != null) {
            for (Long id : dto.getImageIdDelete()) {
                ProductImage image = productImageRepository.findById(id)
                        .orElseThrow();

                imageStorageService.delete(image.getPublicId());
                productImageRepository.delete(image);
            }
        }

        // ADD ảnh mới
        if (dto.getImages() != null) {
            for (MultipartFile file : dto.getImages()) {
                if (file.isEmpty()) continue;

                ImageUploadResult upload = imageStorageService.upload(file);

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(upload.getUrl());
                image.setPublicId(upload.getPublicId());

                productImageRepository.save(image);
            }
        }
    }


    @Override
    public List<ProductImage> getImagesByProductId(Long id) {
        return productImageRepository.findByProductId(id);
    }

    @Override
    public List<OptionValue> getProductOptionValuesByProductId(Long id) {
//        List<OptionValue> values = optionValueRepository.findProductOptionValueByProductId(id);
        return new ArrayList<>();
    }

    @Override
    public Page<ProductClientDto> getProductBySoldDesc(Pageable pageable) {
        Page<Product> products = productRepository.findProductsByIsActiveTrue(pageable);
        return products.map(this::convertToDto);
    }

    @Override
    public Page<ProductClientDto> getProductByCreatedAt(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productClientDtos = productRepository.findProductsByIsActiveTrue(pageable);
        return productClientDtos.map(this::convertToDto);
    }

    @Override
    public Page<ProductClientDto> getProductByCategoryName(String categoryName, Pageable pageable) {
        Category root = categoryRepository.findByName(categoryName);
        List<Long> categoryIds = getAllCategoryIds(root);
        Page<Product> products = productRepository.findProductsByCategoryIdInAndIsActiveTrue(categoryIds, pageable);
        return products.map(this::convertToDto);
    }


    public List<Long> getAllCategoryIds(Category root) {
        List<Long> ids = new ArrayList<>();
        ids.add(root.getId()); // luôn include root

        if (root.getChildren() != null) {
            for (Category child : root.getChildren()) {
                ids.addAll(getAllCategoryIds(child));
            }
        }
        return ids;
    }


}
