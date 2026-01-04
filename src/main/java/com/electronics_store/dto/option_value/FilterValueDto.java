package com.electronics_store.dto.option_value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class FilterValueDto {
    private Long id;    // ID của OptionValue (Dùng để filter)
    private String value; // Text hiển thị (Red, XL)
}
