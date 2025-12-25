package com.electronics_store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
        name = "product_option_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_value_option_value",
                        columnNames = {"option_id", "value"}
                )
        })
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductOptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption productOption;

    @Column(nullable = false, length = 100)
    private String value;
}
