package com.electronics_store.dto.coupon;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateDto {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must contain only uppercase letters and numbers")
    private String code;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @NotBlank(message = "Discount type is required")
    private String discountType; // FIXED, PERCENT

    @DecimalMin(value = "0", message = "Minimum order amount must be non-negative")
    private BigDecimal minOrderAmount;

    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private boolean active = true;
}