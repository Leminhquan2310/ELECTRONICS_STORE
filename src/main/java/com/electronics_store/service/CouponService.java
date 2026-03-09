package com.electronics_store.service;

import com.electronics_store.dto.coupon.CouponCreateDto;
import com.electronics_store.dto.coupon.CouponUpdateDto;
import com.electronics_store.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {
    Coupon validateAndGetCoupon(String code, BigDecimal currentTotal);

    BigDecimal calculateDiscount(Coupon coupon, BigDecimal subTotal);

    Page<Coupon> findWithFilters(String keyword, String status, Pageable pageable);

    void createCoupon(CouponCreateDto dto);

    void updateCoupon(Long id, CouponUpdateDto dto);

    Coupon findById(Long id);

    void deleteCoupon(Long id);

    void toggleStatus(Long id);

    CouponUpdateDto convertToUpdateDto(Coupon coupon);
}
