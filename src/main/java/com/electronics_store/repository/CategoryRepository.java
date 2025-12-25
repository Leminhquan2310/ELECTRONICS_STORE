package com.electronics_store.repository;

import com.electronics_store.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value = "SELECT COALESCE(MAX(c.sort_order), 0) from categories c WHERE c.parent_id = :parentId", nativeQuery = true)
    Integer findMaxSortOrderByParenId(@Param("parentId") Long parentId);

    @Query(value = "SELECT COALESCE(MAX(c.sort_order), 0)    FROM categories c  WHERE c.parent_id IS NULL", nativeQuery = true)
    Integer findMaxSortOrderRoot();

    boolean existsBySlugAndIsActiveTrue(String slug);

    Optional<Category> findById(Long id);

    List<Category> findByParentIdOrderBySortOrder(Long parentId);

    List<Category> findByIsActiveTrue();

    List<Category> findByIsActiveTrueAndIsLeafTrue();
}

