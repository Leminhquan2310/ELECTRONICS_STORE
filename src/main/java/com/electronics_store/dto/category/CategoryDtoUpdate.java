package com.electronics_store.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryDtoUpdate {
    @NotNull
    private Long id;

    @Size(min = 5, max = 150, message = "Name must be between 5 and 150")
    private String name;

    @Size(min = 5, max = 150, message = "Slug must be between 5 and 150")
    private String slug;

    @Min(value = 0, message = "Parent must be greater than 0")
    private Long parent;
}