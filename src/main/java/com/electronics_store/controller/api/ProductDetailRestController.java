package com.electronics_store.controller.api;

import com.electronics_store.model.ProductVariant;
import com.electronics_store.service.ProductOptionService;
import com.electronics_store.service.ProductService;
import com.electronics_store.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailRestController {
    @Autowired
    private ProductOptionService productOptionService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("product", productService.getProductForClient(id));
        result.put("options", productOptionService.getOptionsByProductId(id));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-stock")
    public ResponseEntity<?> getStock(@RequestParam Long productId, @RequestParam List<Long> optionValueIds) {
        ProductVariant productVariant = productVariantService.getByProductIdAndOptionValues(productId, optionValueIds);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("stock", productVariant.getStockQuantity());
        return ResponseEntity.ok(response);
    }
}
