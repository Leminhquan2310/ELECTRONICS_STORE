package com.electronics_store.repository;

import com.electronics_store.model.Category;
import com.electronics_store.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    // Lấy Product active kèm phân trang
    @EntityGraph(attributePaths = {"images", "category"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    List<Product> findProductsByIsActiveTrue();

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findProductsByIsActiveTrue(Pageable pageable);

    Page<Product> findProductsByCategoryIdInAndIsActiveTrue(Collection<Long> categoryIds, Pageable pageable);

    boolean existsByCategory(Category category);
}

