package com.electronics_store.service.impl;

import com.electronics_store.dto.wishlist.WishlistDto;
import com.electronics_store.dto.wishlist.WishlistItemDto;
import com.electronics_store.model.Product;
import com.electronics_store.model.User;
import com.electronics_store.model.Wishlist;
import com.electronics_store.repository.ProductRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.repository.WishlistRepository;
import com.electronics_store.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public boolean addToWishlist(Long userId, Long productId) {
        try {
            // Check if already in wishlist
            if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
                return false; // Already exists
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product).
                    build();
            wishlistRepository.save(wishlist);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add to wishlist: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean removeFromWishlist(Long userId, Long productId) {
        try {
            if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
                return false; // Not in wishlist
            }

            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove from wishlist: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<WishlistItemDto> getUserWishlist(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUserIdWithProduct(userId);

        return wishlistItems.stream()
                .map(this::convertToWishlistItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WishlistItemDto> getUserWishlist(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);

        return wishlistPage.map(this::convertToWishlistItemDto);
    }

    @Override
    public long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public void clearWishlist(Long userId) {
        wishlistRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public boolean moveToCart(Long userId, Long productId) {
        // Implementation depends on your CartService
        // This is optional
        return false;
    }

    @Override
    public List<WishlistDto> checkProductsInWishlist(Long userId, List<Long> productIds) {
        return productIds.stream()
                .map(productId -> {
                    WishlistDto dto = new WishlistDto();
                    dto.setProductId(productId);
                    dto.setInWishlist(isInWishlist(userId, productId));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to DTO
    private WishlistItemDto convertToWishlistItemDto(Wishlist wishlist) {
        WishlistItemDto dto = new WishlistItemDto();
        Product product = wishlist.getProduct();

        dto.setId(wishlist.getId());
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductImage(product.getImages().get(0).getImageUrl());
        dto.setPrice(product.getBasePrice());
        dto.setBasePrice(product.getBasePrice());
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        return dto;
    }
}