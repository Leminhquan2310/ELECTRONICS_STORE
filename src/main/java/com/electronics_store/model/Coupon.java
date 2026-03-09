package com.electronics_store.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Ví dụ: "GIAM20", "SUMMER2026"

    @Column(nullable = false)
    private BigDecimal discountValue; // Giá trị giảm (Ví dụ: 50.00 hoặc 10%)

    @Column(nullable = false)
    private String discountType; // "FIXED" (số tiền cố định) hoặc "PERCENT" (%)

    private BigDecimal minOrderAmount; // Đơn hàng tối thiểu để áp dụng

    private LocalDateTime expiryDate; // Ngày hết hạn

    private Integer usageLimit; // Tổng số lần mã có thể dùng

    private Integer usedCount;  // Số lần đã dùng thực tế

    @Column(nullable = false)
    private boolean active = true;

    // Kiểm tra xem coupon có còn hợp lệ không
    public boolean isValid(BigDecimal orderAmount) {
        return active &&
                (expiryDate == null || expiryDate.isAfter(LocalDateTime.now())) &&
                (usageLimit == null || usedCount < usageLimit) &&
                (minOrderAmount == null || orderAmount.compareTo(minOrderAmount) >= 0);
    }
}