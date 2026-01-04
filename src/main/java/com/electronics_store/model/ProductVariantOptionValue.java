package com.electronics_store.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_variant_option_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_variant_option_value",
                        columnNames = {"product_variant_id", "option_value_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Variant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant variant;

    // Option value (Color=Black, Size=M)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id", nullable = false)
    private OptionValue optionValue;
}
