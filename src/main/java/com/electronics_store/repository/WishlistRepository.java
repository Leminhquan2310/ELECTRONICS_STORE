package com.electronics_store.repository;

import com.electronics_store.model.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // Check if product exists in user's wishlist
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Find wishlist item by user and product
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    // Get all wishlist items for a user with pagination
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    // Get all wishlist items for a user
    List<Wishlist> findByUserId(Long userId);

    // Count wishlist items for a user
    long countByUserId(Long userId);

    // Delete wishlist item by user and product
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    // Find wishlist items with product details
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p WHERE w.user.id = :userId")
    List<Wishlist> findByUserIdWithProduct(@Param("userId") Long userId);

    // Find wishlist items with product details (paged)
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p WHERE w.user.id = :userId")
    Page<Wishlist> findByUserIdWithProduct(@Param("userId") Long userId, Pageable pageable);

    // Clear user's wishlist
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByProductId(Long id);
}
