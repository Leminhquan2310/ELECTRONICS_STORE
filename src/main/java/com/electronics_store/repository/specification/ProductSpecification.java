package com.electronics_store.repository.specification;

import com.electronics_store.model.Category;
import com.electronics_store.model.OptionValue;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductOption;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> filterProducts(String keyword, List<Long> categoryIds, List<Long> optionValueIds) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Chỉ lấy sản phẩm Active
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            // 2. Lọc theo Keyword (Tên hoặc Mô tả)
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
                ));
            }

            // 3. Category Filter (Dùng danh sách ID lá đã "bung" từ Service)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                // Chỉ cần 1 dòng này thay vì vòng lặp for
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // 4. Lọc theo OptionValues (Màu, Size...) thông qua bảng ProductOption
            if (optionValueIds != null && !optionValueIds.isEmpty()) {
                // Join từ Product -> ProductOption
                Join<Product, ProductOption> productOptionJoin = root.join("productOptions");
                // Join từ ProductOption -> OptionValue
                Join<ProductOption, OptionValue> optionValueJoin = productOptionJoin.join("optionValue");

                // Điều kiện: option_value_id nằm trong danh sách đã chọn
                predicates.add(optionValueJoin.get("id").in(optionValueIds));

                // Quan trọng: Vì 1 sản phẩm join ra nhiều dòng option, cần distinct để không bị trùng sản phẩm
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
