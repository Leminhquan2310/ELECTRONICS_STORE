package com.electronics_store.service.impl;

import com.electronics_store.dto.image.ImageUploadResult;
import com.electronics_store.dto.option.OptionInputDto;
import com.electronics_store.dto.product.*;
import com.electronics_store.helper.VariantGenerator;
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
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;

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

    private void mapDataProduct(Product product, ProductDtoUpdate productDtoUpdate, Category category) {
        product.setName(productDtoUpdate.getName());
        product.setDescription(productDtoUpdate.getDescription());
        product.setBasePrice(productDtoUpdate.getBasePrice());
        product.setCategory(category);
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
            Category category = categoryRepository.getReferenceById(productDtoUpdate.getCategoryId());

            // 1. Map thông tin cơ bản
            mapDataProduct(product, productDtoUpdate, category);

            // 2. Cập nhật ảnh
            updateImages(product, productDtoUpdate);

            // 3. Đồng bộ Options và OptionValues (Smart Sync)
            addMoreNewOptions(product, productDtoUpdate.getOptions());

            // 5. Đồng bộ Biến thể (Smart Sync Variants)
            updateVariant(product);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private void addMoreNewOptions(Product product, List<OptionInputDto> newOptions) {
        // Lấy list hiện tại từ DB
        List<ProductOption> currentOptions = product.getProductOptions();

        // Tạo map để kiểm tra cho nhanh: "optionId-valueId"
        Set<String> newKeys = new HashSet<>();
        for (OptionInputDto input : newOptions) {
            for (Long valId : input.getValueIds()) {
                newKeys.add(input.getOptionId() + "-" + valId);
            }
        }

//         1. Xóa những OptionValue không còn được chọn
        currentOptions.removeIf(existing -> {
            String key = existing.getOption().getId() + "-" + existing.getOptionValue().getId();
            return !newKeys.contains(key);
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

    @Override
    public boolean delete(Long id) {
        Product product = productRepository.findById(id).get();
        if (checkCanDelete(id)) {
            productRepository.delete(product);
            return true;
        }

        product.setIsActive(false);
        product.getVariants().forEach(v -> v.setActive(false));
        productRepository.save(product);
        return true;
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
        // Check if product exists in wishlists
        if (wishlistRepository.existsByProductId(id)) {
            return false;
        }

        // Check if product has reviews
        if (productReviewRepository.existsByProductId(id)) {
            return false;
        }

        // Check if product exists in any cart items
        if (cartItemRepository.existsByProductId(id)) {
            return false;
        }

        // Check if product exists in any order items
        return !orderItemRepository.existsByProductId(id);
    }

    public void updateVariant(Product product) {
        // 1. Sinh ra các tổ hợp mong muốn hiện tại (từ ProductOption mới update)
        List<List<OptionValue>> combinations = generateCombinationsFromProduct(product);

        // 2. Lấy danh sách Variant hiện có trong DB
        List<ProductVariant> existingVariants = product.getVariants();

        // 3. Tạo Map SKU hoặc Map tập hợp OptionValue ID để so sánh
        Map<String, ProductVariant> existingMap = existingVariants.stream()
                .collect(Collectors.toMap(this::getVariantKey, v -> v));

        Set<String> processedKeys = new HashSet<>();

        for (List<OptionValue> combo : combinations) {
            String key = getComboKey(combo);
            processedKeys.add(key);

            if (existingMap.containsKey(key)) {
                // Nếu đã tồn tại tổ hợp này: Active lại nếu nó đang bị disable
                ProductVariant v = existingMap.get(key);
                v.setActive(true);
            } else {
                // Nếu là tổ hợp mới hoàn toàn: Thêm mới
                ProductVariant newV = new ProductVariant();
                newV.setProduct(product);
                newV.setPriceAdjustment(product.getBasePrice());
                newV.setStockQuantity(0);
                newV.setSku(generateSku(product.getName(), combo));
                combo.forEach(newV::addOptionValue);
                existingVariants.add(newV);
            }
        }

        // 7. Những Variant cũ không còn nằm trong tổ hợp mới -> Delete
        Iterator<ProductVariant> it = existingVariants.iterator();
        while (it.hasNext()) {
            ProductVariant v = it.next();
            if (!processedKeys.contains(getVariantKey(v))) {
                if (canDeleteVariant(v)) {
                    v.getVariantValues().clear(); // clear join table
                    it.remove();
                } else {
                    v.setActive(false);
                }
            }
        }

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

    // Hàm check xem Variant này đã có đơn hàng chưa
    private boolean canDeleteVariant(ProductVariant v) {
        boolean existsInOrderItem = orderItemRepository.existsByProductVariant(v);
        boolean existInProductReview = false;
        boolean existInCartItem = cartItemRepository.existsByVariant(v);
        return !existsInOrderItem && !existInProductReview && !existInCartItem;
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
    public Page<ProductClientDto> getProductBySoldDesc(Pageable pageable) {
        Page<Product> products = productRepository.findProductsByIsActiveTrue(pageable);
        return products.map(this::convertToDto);
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
