package com.electronics_store.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CategoryDtoCreate {
    @Size(min = 5, max = 150, message = "Name must be between 5 and 150")
    private String name;

    @Size(min = 5, max = 150, message = "Slug must be between 5 and 150")
    private String slug;

    @Min(value = 0, message = "Parent must be greater than 0")
    private Long parent;
}
