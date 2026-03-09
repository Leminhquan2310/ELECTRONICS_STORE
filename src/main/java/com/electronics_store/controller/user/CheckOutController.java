package com.electronics_store.controller.user;

import com.electronics_store.dto.checkout.CheckoutRequestDto;
import com.electronics_store.model.CartItem;
import com.electronics_store.model.Order;
import com.electronics_store.model.User;
import com.electronics_store.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user/checkout")
public class CheckOutController {
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private UserService userService;
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private VnPayService vnPayService;

    @GetMapping("")
    public ModelAndView showCheckout() {
        return new ModelAndView("client/checkout");
    }


    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(
            @RequestBody CheckoutRequestDto checkoutRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Please login to place order.");
        }

        try {
            // Lấy Entity User thực từ Database dựa trên email/username trong session
            User currentUser = userService.getCurrentUser();
            Order order = checkoutService.placeOrder(checkoutRequest, currentUser);

            // 2. Kiểm tra phương thức thanh toán
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());

            if ("DIRECT_BANK_TRANSFER".equals(checkoutRequest.getPaymentMethod())) {
                // == Xử lý VNPay ==
                BigDecimal exchangeRate = exchangeRateService.usdToVnd();
                BigDecimal totalAmountVNDDecimal = order.getTotalAmount().multiply(exchangeRate);
                long totalAmountVND = totalAmountVNDDecimal.setScale(0, RoundingMode.HALF_UP).longValue();

                String paymentUrl = vnPayService.createPaymentUrl(
                        request,
                        totalAmountVND,
                        checkoutRequest.getBankCode(),
                        order.getId()
                );

                response.put("redirectUrl", paymentUrl); // Frontend sẽ redirect theo link này
                response.put("message", "Đang chuyển hướng sang VNPay...");

            } else {
                // == Xử lý COD hoặc Default ==
                response.put("redirectUrl", "/user/congratulation"); // Trang cảm ơn
                response.put("message", "Order placed successfully!");
            }

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


}
