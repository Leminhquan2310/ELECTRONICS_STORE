package com.electronics_store.service;

import com.electronics_store.dto.cart.AddToCartDto;
import com.electronics_store.dto.cart.CartSummaryDto;
import com.electronics_store.model.Cart;
import com.electronics_store.model.CartItem;
import com.electronics_store.model.User;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    Cart getActiveCart();

    void addToCart(AddToCartDto addToCartDto);

    void updateQuantity(Long cartItemId, int quantity);

    void removeItem(Long cartItemId);

    CartSummaryDto getCartSummary();

    List<CartItem> getCartItems(User user);

    void clearCart(User user);

    void checkout();
}

