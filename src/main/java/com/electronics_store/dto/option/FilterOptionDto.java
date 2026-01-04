package com.electronics_store.dto.option;

import com.electronics_store.dto.option_value.FilterValueDto;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class FilterOptionDto {
    private String name;        // Tên Option (Ví dụ: Color)
    private List<FilterValueDto> values; // List các giá trị (Red, Blue)
}
