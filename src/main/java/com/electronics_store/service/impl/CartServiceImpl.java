package com.electronics_store.service.impl;

import com.electronics_store.dto.cart.*;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.*;
import com.electronics_store.repository.CartItemRepository;
import com.electronics_store.repository.CartRepository;
import com.electronics_store.repository.ProductRepository;
import com.electronics_store.repository.ProductVariantRepository;
import com.electronics_store.service.CartService;
import com.electronics_store.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @Autowired
    private ProductMapper productMapper;


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

        // 1. Lấy cart active (login only)
        Cart cart = getActiveCart();

        // 2. Resolve variant từ optionValueIds
        ProductVariant variant = productVariantRepository
                .findVariantByOptionValues(
                        request.getProductId(),
                        request.getOptionValueIds(),
                        request.getOptionValueIds().size()
                )
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy biến thể phù hợp"));

        // 3. Delegate xử lý chi tiết
        addToCartInternal(cart, variant, request.getQuantity());
    }

    private void addToCartInternal(Cart cart, ProductVariant variant, int quantity) {

        // 1. Tồn kho
        int stock = variant.getStock_quantity();

        // 2. Kiểm tra item đã tồn tại chưa
        CartItem item = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElse(null);

        int newQuantity = (item == null)
                ? quantity
                : item.getQuantity() + quantity;

        if (newQuantity > stock) {
            throw new RuntimeException("Vượt quá tồn kho");
        }

        if (item == null) {
            // 3. Tạo mới
            item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(quantity);
            item.setProduct(variant.getProduct());
            // snapshot giá tại thời điểm add
            item.setPriceAtTime(variant.getPriceAdjustment());
        } else {
            // 4. Cộng dồn
            item.setQuantity(newQuantity);
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

        int stock = item.getVariant() != null ? item.getVariant().getStock_quantity() : 0;

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
            dto.setProduct(productMapper.toClientDto(item.getProduct()));
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
    public void checkout() {
        Cart cart = getActiveCart();

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        cart.setActive(false);
    }
}
