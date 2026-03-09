package com.electronics_store.repository;

import com.electronics_store.model.OrderItem;
import com.electronics_store.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProductVariant(ProductVariant productVariant);

    boolean existsByProductId(Long id);
}
