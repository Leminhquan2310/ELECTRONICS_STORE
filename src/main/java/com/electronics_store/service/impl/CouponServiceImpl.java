package com.electronics_store.service.impl;

import com.electronics_store.dto.coupon.CouponCreateDto;
import com.electronics_store.dto.coupon.CouponUpdateDto;
import com.electronics_store.model.Coupon;
import com.electronics_store.repository.CouponRepository;
import com.electronics_store.service.CouponService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
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

    @Override
    public Page<Coupon> findWithFilters(String keyword, String status, Pageable pageable) {
        Specification<Coupon> spec = Specification.where(null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("code")), "%" + keyword.toLowerCase() + "%"));
        }

        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                spec = spec.and((root, query, cb) -> cb.isTrue(root.get("active")));
            } else if ("inactive".equals(status)) {
                spec = spec.and((root, query, cb) -> cb.isFalse(root.get("active")));
            } else if ("expired".equals(status)) {
                spec = spec.and((root, query, cb) ->
                        cb.and(
                                cb.isNotNull(root.get("expiryDate")),
                                cb.lessThan(root.get("expiryDate"), LocalDateTime.now())
                        ));
            }
        }

        return couponRepository.findAll(spec, pageable);
    }

    @Override
    public void createCoupon(CouponCreateDto dto) {
        // Kiểm tra mã code đã tồn tại
        if (couponRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(dto.getCode().toUpperCase())
                .discountValue(dto.getDiscountValue())
                .discountType(dto.getDiscountType())
                .minOrderAmount(dto.getMinOrderAmount())
                .expiryDate(dto.getExpiryDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .active(dto.isActive())
                .build();

        couponRepository.save(coupon);
    }

    @Override
    public void updateCoupon(Long id, CouponUpdateDto dto) {
        Coupon coupon = findById(id);

        // Kiểm tra mã code đã tồn tại (trừ chính nó)
        if (!coupon.getCode().equalsIgnoreCase(dto.getCode()) &&
                couponRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        coupon.setCode(dto.getCode().toUpperCase());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setMinOrderAmount(dto.getMinOrderAmount());
        coupon.setExpiryDate(dto.getExpiryDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setActive(dto.isActive());

        couponRepository.save(coupon);
    }

    @Override
    public Coupon findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found"));
    }

    @Override
    public void deleteCoupon(Long id) {
        Coupon coupon = findById(id);
        // Chỉ cho phép xóa nếu chưa được sử dụng
        if (coupon.getUsedCount() > 0) {
            throw new IllegalStateException("Cannot delete coupon that has been used");
        }
        couponRepository.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id) {
        Coupon coupon = findById(id);
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }

    public CouponUpdateDto convertToUpdateDto(Coupon coupon) {
        return CouponUpdateDto.builder()
                .code(coupon.getCode())
                .discountValue(coupon.getDiscountValue())
                .discountType(coupon.getDiscountType())
                .minOrderAmount(coupon.getMinOrderAmount())
                .expiryDate(coupon.getExpiryDate())
                .usageLimit(coupon.getUsageLimit())
                .active(coupon.isActive())
                .build();
    }

}
