package com.electronics_store.repository;

import com.electronics_store.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    @Query(value = "SELECT pv.* FROM product_variants pv " +
            "JOIN product_variant_option_values pvov ON pv.id = pvov.variant_id " +
            "WHERE pv.product_id = :productId " +
            "GROUP BY pv.id " +
            "HAVING COUNT(pvov.option_value_id) = :size " +
            "AND SUM( CASE WHEN pvov.option_value_id " +
            "IN (:optionValueIds) THEN 1 ELSE 0 END ) = :size", nativeQuery = true)
    Optional<ProductVariant> findVariantByOptionValues(
            @Param("productId") Long productId,
            @Param("optionValueIds") List<Long> optionValueIds,
            @Param("size") long size
    );
}
