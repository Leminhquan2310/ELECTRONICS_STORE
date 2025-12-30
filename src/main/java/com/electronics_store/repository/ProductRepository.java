package com.electronics_store.repository;

import com.electronics_store.model.Category;
import com.electronics_store.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findProductsByIsActiveTrue();

    Page<Product> findProductsByIsActiveTrue(Pageable pageable);

    Page<Product> findProductsByCategoryIdInAndIsActiveTrue(Collection<Long> categoryIds, Pageable pageable);
}

