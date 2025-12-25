package com.electronics_store.dto.product;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductOptionDtoUpdate {

    private Long id;

    @NotBlank(message = "Cannot be left blank")
    private String name;

    @Size(min = 1, message = "Option must have at least 1 character")
    private List<ProductOptionValueDtoUpdate> values;
}
