package com.electronics_store.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 150, unique = true)
    private String slug;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    private int level = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_leaf")
    private Boolean isLeaf = true;

    public Category(@Min(value = 0, message = "Parent must be greater than 0") Long parent) {
    }
}
