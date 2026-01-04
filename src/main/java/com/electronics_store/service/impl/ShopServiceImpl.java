package com.electronics_store.service.impl;

import com.electronics_store.dto.option.FilterOptionDto;
import com.electronics_store.dto.option_value.FilterValueDto;
import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.model.Category;
import com.electronics_store.model.Option;
import com.electronics_store.model.Product;
import com.electronics_store.repository.CategoryRepository;
import com.electronics_store.repository.OptionRepository;
import com.electronics_store.repository.ProductRepository;
import com.electronics_store.repository.specification.ProductSpecification;
import com.electronics_store.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private CategoryRepository categoryRepository; // Giả sử bạn đã có repo này

    @Override
    public Page<ProductClientDto> filterProducts(String keyword, List<Long> rootCategoryIds, List<Long> optionValueIds, int page, int size, String sortBy) {
        Sort sort;
        switch (sortBy) {
            case "name-asc":
                sort = Sort.by(Sort.Direction.ASC, "name");
                break;
            case "name-desc":
                sort = Sort.by(Sort.Direction.DESC, "name");
                break;
            case "price-asc":
                sort = Sort.by(Sort.Direction.ASC, "basePrice");
                break;
            case "price-desc":
                sort = Sort.by(Sort.Direction.DESC, "basePrice");
                break;
            case "rating-desc":
                sort = Sort.by(Sort.Direction.DESC, "ratingAvg");
                break;
            case "trending":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Mặc định hàng mới về
                break;
        }

        List<Long> allTargetCategoryIds = new ArrayList<>();

        if (rootCategoryIds != null && !rootCategoryIds.isEmpty()) {
            for (Long rootId : rootCategoryIds) {
                // Tìm tất cả các category con cháu là nút lá thuộc Root này
                List<Long> leafIds = findAllLeafIdsByParent(rootId);
                allTargetCategoryIds.addAll(leafIds);
            }
        }

        Specification<Product> spec = ProductSpecification.filterProducts(keyword, allTargetCategoryIds, optionValueIds);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(this::convertToDto);
    }

    private List<Long> findAllLeafIdsByParent(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        List<Long> leafIds = new ArrayList<>();

        if (children.isEmpty()) {
            // Nếu không có con, chính nó là lá
            leafIds.add(parentId);
        } else {
            for (Category child : children) {
                if (child.isLeaf()) {
                    leafIds.add(child.getId());
                } else {
                    // Nếu chưa phải lá, tiếp tục đào sâu xuống
                    leafIds.addAll(findAllLeafIdsByParent(child.getId()));
                }
            }
        }
        return leafIds;
    }

    // Lấy dữ liệu Sidebar (Categories & Dynamic Options)
    @Override
    public List<FilterOptionDto> getFilterOptions() {
        List<Option> options = optionRepository.findAllOptionsWithValues();

        // Convert Entity sang DTO
        return options.stream().map(opt -> new FilterOptionDto(
                opt.getName(),
                opt.getValues().stream()
                        .map(ov -> new FilterValueDto(ov.getId(), ov.getValue()))
                        .collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    // Helper: Convert Entity -> DTO
    private ProductClientDto convertToDto(Product product) {
        String imgUrl = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl() : "placeholder.jpg";

        return ProductClientDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .salePrice(product.getBasePrice())
                .imageMain(imgUrl)
                .ratingAvg(product.getRatingAvg())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
