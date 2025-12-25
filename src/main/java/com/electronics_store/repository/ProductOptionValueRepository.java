package com.electronics_store.repository;

import com.electronics_store.model.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    @Query(value = " SELECT pov.* FROM product_option_values pov JOIN product_options po ON pov.option_id = po.id JOIN products p ON po.product_id = p.id WHERE p.id = :productId ORDER BY pov.id ASC", nativeQuery = true)
    List<ProductOptionValue> findProductOptionValueByProductId(@Param("productId") Long productId);

    List<ProductOptionValue> findByProductOptionId(Long optionId);
}
