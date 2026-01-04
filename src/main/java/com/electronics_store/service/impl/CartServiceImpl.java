package com.electronics_store.service.impl;

import com.electronics_store.dto.cart.*;
import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.*;
import com.electronics_store.repository.CartItemRepository;
import com.electronics_store.repository.CartRepository;
import com.electronics_store.repository.ProductVariantRepository;
import com.electronics_store.service.CartService;
import com.electronics_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private UserService userService;


    @Override
    public Cart getActiveCart() {
        User user = userService.getCurrentUser();

        return cartRepository
                .findByUserAndActiveTrue(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setActive(true);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public void addToCart(AddToCartDto request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        Cart cart = getActiveCart();

        // 1. Tìm biến thể phù hợp dựa trên danh sách Option IDs
        // Phải truyền size của list để đảm bảo tìm đúng biến thể có ĐỦ các thuộc tính đó
        ProductVariant variant = productVariantRepository
                .findVariantByOptionValues(
                        request.getProductId(),
                        request.getOptionValueIds(),
                        (long) request.getOptionValueIds().size()
                )
                .orElseThrow(() -> new RuntimeException("Vui lòng chọn đầy đủ các tùy chọn (Màu sắc, kích thước...)"));

        addToCartInternal(cart, variant, request.getQuantity());
    }

    private void addToCartInternal(Cart cart, ProductVariant variant, int quantity) {
        if (variant.getStockQuantity() < quantity) {
            throw new RuntimeException("Sản phẩm hiện chỉ còn " + variant.getStockQuantity() + " món.");
        }

        CartItem item = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setProduct(variant.getProduct());
            item.setQuantity(quantity);

            // 2. Tính giá snapshot: Base Price của Product + Price Adjustment của Variant
            BigDecimal basePrice = variant.getProduct().getBasePrice();
            item.setPriceAtTime(basePrice);

        } else {
            // 3. Cộng dồn số lượng
            int totalNewQuantity = item.getQuantity() + quantity;
            if (totalNewQuantity > variant.getStockQuantity()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho cho phép.");
            }
            item.setQuantity(totalNewQuantity);
        }

        cartItemRepository.save(item);
    }

    @Override
    public void updateQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải >= 1");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        int stock = item.getVariant() != null ? item.getVariant().getStockQuantity() : 0;

        if (quantity > stock) {
            throw new RuntimeException("Vượt quá tồn kho");
        }

        item.setQuantity(quantity);
    }

    @Transactional
    @Override
    public void removeItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found: " + cartItemId));

        // Nếu có relationship, clear trước
        if (cartItem.getCart() != null) {
            Cart cart = cartItem.getCart();
            cart.getItems().remove(cartItem);
            cartItem.setCart(null);
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public CartSummaryDto getCartSummary() {
        Cart cart = getActiveCart();
        List<CartItemDto> items = cart.getItems().stream().map(item -> {
            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId());
            dto.setProduct(mapToProductClientDto(item.getProduct()));
            dto.setVariantSku(
                    item.getVariant() != null
                            ? item.getVariant().getSku()
                            : null
            );
            dto.setPrice(item.getPriceAtTime());
            dto.setQuantity(item.getQuantity());
            dto.setTotal(
                    item.getPriceAtTime()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            return dto;
        }).toList();

        BigDecimal subTotal = items.stream()
                .map(CartItemDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartSummaryDto summary = new CartSummaryDto();
        summary.setItems(items);
        summary.setSubTotal(subTotal);

        return summary;
    }

    @Override
    public List<CartItem> getCartItems(User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            return new ArrayList<>();
        }
        return cart.getItems();
    }

    @Transactional
    @Override
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
             cart.getItems().clear();
             cartRepository.save(cart);
        }
    }

    private ProductClientDto mapToProductClientDto(Product product) {
        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        return ProductClientDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .salePrice(product.getBasePrice())
                .images(imageUrls)
                .imageMain(imageUrls.get(0))
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .categoryName(
                        product.getCategory() != null
                                ? product.getCategory().getName()
                                : null
                )
                .build();
    }

    @Override
    public void checkout() {
        Cart cart = getActiveCart();

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        cart.setActive(false);
    }
}
