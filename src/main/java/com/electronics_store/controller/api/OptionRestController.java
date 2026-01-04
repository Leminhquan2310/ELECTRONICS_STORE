package com.electronics_store.controller.api;

import com.electronics_store.dto.option.OptionDto;
import com.electronics_store.service.OptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/options")
public class OptionRestController {
    @Autowired
    private OptionService optionService;

    @GetMapping("")
    public ResponseEntity<List<OptionDto>> getOptions() {
        return ResponseEntity.ok(optionService.findAll());
    }

    @PostMapping("")
    public ResponseEntity<?> createOption(@RequestBody Map<String, String> payload) {
        if (optionService.isDuplicate(payload.get("name"))) {
            return ResponseEntity.badRequest().build();
        }
        optionService.create(payload.get("name"));
        return ResponseEntity.ok().build();
    }

    //  Check mức độ ảnh hưởng
    @GetMapping("/{id}/usage")
    public ResponseEntity<Long> checkUsage(@PathVariable Long id) {
        return ResponseEntity.ok(optionService.getUsageCount(id));
    }

    // Thực hiện cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOption(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (optionService.isDuplicate(name)) {
            return ResponseEntity.badRequest().build();
        }
        optionService.updateOption(id, payload.get("name"));
        return ResponseEntity.ok().build();
    }
}
