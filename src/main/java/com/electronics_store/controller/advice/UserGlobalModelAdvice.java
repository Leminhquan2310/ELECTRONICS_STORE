package com.electronics_store.controller.advice;

import com.electronics_store.controller.auth.AuthController;
import com.electronics_store.dto.cart.CartSummaryDto;
import com.electronics_store.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = {"com.electronics_store.controller.client",
        "com.electronics_store.controller.user",
        "com.electronics_store.controller.auth"})
public class UserGlobalModelAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute("cart")
    public CartSummaryDto cart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated and not anonymous
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return new CartSummaryDto();
        }

        return cartService.getCartSummary();
    }
}
