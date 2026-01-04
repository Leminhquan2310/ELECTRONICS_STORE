package com.electronics_store.repository;

import com.electronics_store.model.Option;
import com.electronics_store.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    @Query(value = "SELECT count(distinct po.product.id) FROM ProductOption po where po.option.id = :optionId")
    Long countProductsUsingOption(@Param("optionId") Long optionId);

    List<ProductOption> findByProductId(Long id);

    void deleteByProductId(Long id);

    @Query("SELECT po FROM ProductOption po " +
            "JOIN FETCH po.option " +
            "JOIN FETCH po.optionValue " +
            "WHERE po.product.id = :productId")
    List<ProductOption> findAllByProductIdWithDetails(@Param("productId") Long productId);
}
