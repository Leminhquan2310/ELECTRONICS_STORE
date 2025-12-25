package com.electronics_store.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDtoCreate {

    @NotBlank(message = "Cannot be left blank ")
    private String name;

    @NotBlank(message = "Cannot be left blank")
    private String description;

    @NotNull(message = "Cannot be left blank")
    private BigDecimal basePrice;

    @NotNull(message = "Cannot be left blank")
    private Long categoryId;

    @Size(min = 0)
    private List<ProductOptionDtoCreate> options;

    @Size(min = 0)
    List<MultipartFile> images; // URL hoặc path sau upload
}