package com.electronics_store.dto.coupon;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUpdateDto {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 20, message = "Code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Code must contain only uppercase letters and numbers")
    private String code;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @NotBlank(message = "Discount type is required")
    private String discountType;

    @DecimalMin(value = "0", message = "Minimum order amount must be non-negative")
    private BigDecimal minOrderAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expiryDate;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    private boolean active;
}