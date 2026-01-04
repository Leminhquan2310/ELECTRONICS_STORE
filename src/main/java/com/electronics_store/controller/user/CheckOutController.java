package com.electronics_store.controller.user;

import com.electronics_store.dto.checkout.CheckoutRequestDto;
import com.electronics_store.model.CartItem;
import com.electronics_store.model.Order;
import com.electronics_store.model.User;
import com.electronics_store.service.CartService;
import com.electronics_store.service.CheckoutService;
import com.electronics_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/user/checkout")
public class CheckOutController {
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private UserService userService;

    @GetMapping("")
    public ModelAndView showCheckout() {
        return new ModelAndView("client/checkout");
    }


    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(
            @RequestBody CheckoutRequestDto checkoutRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để thanh toán.");
        }

        try {
            // Lấy Entity User thực từ Database dựa trên email/username trong session
            User currentUser = userService.getCurrentUser();

            Order order = checkoutService.placeOrder(checkoutRequest, currentUser);

            return ResponseEntity.ok().body("Đặt hàng thành công. Mã đơn: " + order.getId());
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


}
