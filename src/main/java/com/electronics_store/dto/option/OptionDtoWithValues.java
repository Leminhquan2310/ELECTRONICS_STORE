package com.electronics_store.dto.option;

import com.electronics_store.dto.option_value.OptionValueDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionDtoWithValues {
    private Long id;
    private String name;
    private List<OptionValueDto> values;
}
