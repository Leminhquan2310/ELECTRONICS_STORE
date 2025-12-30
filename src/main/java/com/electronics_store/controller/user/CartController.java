package com.electronics_store.controller.user;

import com.electronics_store.dto.cart.AddToCartDto;
import com.electronics_store.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/user/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {
    @Value("${app.base-url}")
    private String baseUrl;

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("baseUrl", baseUrl);
        return "client/cart";
    }

    @PostMapping("/add")
    public String addToCart(@ModelAttribute AddToCartDto request) {
        cartService.addToCart(request);
        return "redirect:/user/cart";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam Long itemId,
            @RequestParam int quantity
    ) {
        cartService.updateQuantity(itemId, quantity);
        return "redirect:/user/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long itemId) {
        cartService.removeItem(itemId);
        return "redirect:/user/cart";
    }
}
