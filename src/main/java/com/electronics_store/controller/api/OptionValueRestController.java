package com.electronics_store.controller.api;

import com.electronics_store.dto.option_value.OptionValueDto;
import com.electronics_store.service.OptionValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ProductOptionRestController.java
@RestController
@RequestMapping("/api/admin/option-values")
public class OptionValueRestController {
    @Autowired
    private OptionValueService optionValueService;

    // API: GET /api/admin/product-options/{optionId}/values
    @GetMapping("/by-option-id/{optionId}")
    public ResponseEntity<List<OptionValueDto>> getValues(@PathVariable Long optionId) {
        List<OptionValueDto> dtos = optionValueService.getValuesByOptionId(optionId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("")
    public ResponseEntity<OptionValueDto> updateOptionValue(@RequestBody Map<String, String> payload) {
        Long optionId = Long.parseLong(payload.get("optionId"));
        String newValue = payload.get("value");
        OptionValueDto optionValueDto = optionValueService.create(optionId, newValue);
        return ResponseEntity.ok(optionValueDto);
    }

    @PutMapping("/values/{valueId}")
    public ResponseEntity<?> updateOptionValue(@PathVariable Long valueId, @RequestBody Map<String, String> payload) {
        String newValue = payload.get("value");
        optionValueService.updateValueName(valueId, newValue);
        return ResponseEntity.ok().build();
    }


}