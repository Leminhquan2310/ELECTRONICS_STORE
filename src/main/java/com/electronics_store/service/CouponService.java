package com.electronics_store.service;

import com.electronics_store.model.Coupon;

import java.math.BigDecimal;

public interface CouponService {
    Coupon validateAndGetCoupon(String code, BigDecimal currentTotal);

    BigDecimal calculateDiscount(Coupon coupon, BigDecimal subTotal);
}
