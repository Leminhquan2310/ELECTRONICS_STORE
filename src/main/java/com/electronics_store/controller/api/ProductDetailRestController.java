package com.electronics_store.controller.api;

import com.electronics_store.service.ProductOptionService;
import com.electronics_store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product-details")
public class ProductDetailRestController {
    @Autowired
    private ProductOptionService productOptionService;
    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long id){
        Map<String, Object> result = new HashMap<>();
        result.put("product", productService.getProductForClient(id));
        result.put("options", productOptionService.getOptionsByProductId(id));
        return ResponseEntity.ok(result);
    }
}
