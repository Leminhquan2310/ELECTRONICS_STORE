package com.electronics_store.service.impl;

import com.electronics_store.model.Coupon;
import com.electronics_store.repository.CouponRepository;
import com.electronics_store.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    private CouponRepository couponRepository;

    @Override
    public Coupon validateAndGetCoupon(String code, BigDecimal currentTotal) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("The discount coupon application was unsuccessful."));

        if (!coupon.isValid(currentTotal)) {
            throw new RuntimeException("The discount coupon application was unsuccessful.");
        }
        return coupon;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subTotal) {
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            // Giảm theo % (Ví dụ: 10% của 200 = 20)
            return subTotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal(100));
        } else {
            // Giảm theo số tiền cố định (Ví dụ: 50.00)
            return coupon.getDiscountValue();
        }
    }
}
