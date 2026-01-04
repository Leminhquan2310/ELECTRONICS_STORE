package com.electronics_store.dto.option;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OptionInputDto {
    private Long optionId;      // ID của option (Màu sắc)
    private List<Long> valueIds; // List ID của values (Xanh, Đỏ...)

}