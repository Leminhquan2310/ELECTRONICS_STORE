package com.electronics_store.controller.api;

import com.electronics_store.model.Coupon;
import com.electronics_store.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponRestController {
    @Autowired
    private CouponService couponService;

    @GetMapping("/apply")
    public ResponseEntity<?> applyCoupon(@RequestParam String code, @RequestParam BigDecimal subTotal) {
        try {
            Coupon coupon = couponService.validateAndGetCoupon(code, subTotal);
            BigDecimal discountAmount = couponService.calculateDiscount(coupon, subTotal);

            Map<String, Object> response = new HashMap<>();
            response.put("code", coupon.getCode());
            response.put("discountAmount", discountAmount);
            response.put("newTotal", subTotal.subtract(discountAmount));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
