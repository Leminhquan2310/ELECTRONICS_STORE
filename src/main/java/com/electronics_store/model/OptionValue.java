package com.electronics_store.model;

import jakarta.persistence.*;
import lombok.*;

@Table(
        name = "option_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_option_value",
                        columnNames = {"option_id", "value"}
                )
        })
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;

    @Column(nullable = false, length = 100)
    private String value;
}
