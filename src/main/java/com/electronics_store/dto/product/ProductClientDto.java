package com.electronics_store.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductClientDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal salePrice; // null nếu không giảm
    private String imageMain; // gallery ảnh
    private List<String> images; // gallery ảnh
    private Double ratingAvg;
    private Integer ratingCount;
    private String categoryName;
}
