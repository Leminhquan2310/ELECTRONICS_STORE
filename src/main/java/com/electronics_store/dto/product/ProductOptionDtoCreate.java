package com.electronics_store.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductOptionDtoCreate {

    @NotBlank(message = "Cannot be left blank")
    private String name;

    @Size(min = 1, message = "Option must have at least 1 character")
    private List<@NotBlank String> values;
}