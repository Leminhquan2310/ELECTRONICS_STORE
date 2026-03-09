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
    List<ProductVariant> findAllByProductIsActiveIsTrue();

    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT pv FROM ProductVariant pv " +
            "JOIN pv.variantValues vvo " + // Join qua bảng trung gian ProductVariantOptionValue
            "WHERE pv.product.id = :productId " +
            "AND vvo.optionValue.id IN :optionValueIds " +
            "GROUP BY pv.id " +
            "HAVING COUNT(vvo.id) = :expectedSize")
    Optional<ProductVariant> findVariantByOptionValues(
            @Param("productId") Long productId,
            @Param("optionValueIds") List<Long> optionValueIds,
            @Param("expectedSize") Long expectedSize);

    void deleteByProductId(Long id);

    @Query(value = "SELECT pv.* FROM product_variants pv " +
            "JOIN product_variant_option_values pvov ON pv.id = pvov.product_variant_id " +
            "WHERE pv.product_id = :productId AND pvov.option_value_id IN :optionValueIds " +
            "GROUP BY pv.id " +
            "HAVING COUNT(pvov.option_value_id) = :size", nativeQuery = true)
    ProductVariant findByProductIdAndOptionValueIds(@Param("productId") Long productId, @Param("optionValueIds") List<Long> optionValueIds, @Param("size") int size);
}
