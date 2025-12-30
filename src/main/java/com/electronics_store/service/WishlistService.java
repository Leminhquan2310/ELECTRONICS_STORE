package com.electronics_store.service;

import com.electronics_store.dto.wishlist.WishlistDto;
import com.electronics_store.dto.wishlist.WishlistItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishlistService {
    // Add product to wishlist
    boolean addToWishlist(Long userId, Long productId);

    // Remove product from wishlist
    boolean removeFromWishlist(Long userId, Long productId);

    // Check if product is in wishlist
    boolean isInWishlist(Long userId, Long productId);

    // Get wishlist items for user
    List<WishlistItemDto> getUserWishlist(Long userId);

    // Get wishlist items for user with pagination
    Page<WishlistItemDto> getUserWishlist(Long userId, Pageable pageable);

    // Get wishlist count for user
    long getWishlistCount(Long userId);

    // Clear user's wishlist
    void clearWishlist(Long userId);

    // Move wishlist to cart (optional feature)
    boolean moveToCart(Long userId, Long productId);

    // Check multiple products if in wishlist
    List<WishlistDto> checkProductsInWishlist(Long userId, List<Long> productIds);
}
