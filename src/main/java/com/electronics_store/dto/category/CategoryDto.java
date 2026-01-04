package com.electronics_store.dto.category;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CategoryDto {
    private Long id;
    private String name;
}
