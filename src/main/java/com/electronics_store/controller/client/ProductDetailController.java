package com.electronics_store.controller.client;

import com.electronics_store.dto.cart.AddToCartDto;
import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductOption;
import com.electronics_store.service.ProductOptionService;
import com.electronics_store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product-detail")
public class ProductDetailController {
    @Autowired
    private ProductOptionService productOptionService;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ModelAndView showProductDetail(@PathVariable Long id){
        ModelAndView mav = new ModelAndView("client/product-detail");
        List<ProductOption> listProductOption = productOptionService.getProductOptionsByProductId(id);
        ProductClientDto product = productMapper.toClientDto((Product) productService.getById(id));
        mav.addObject("BASE_URL", "http://localhost:8080/");
        mav.addObject("addToCartDto", new AddToCartDto());
        mav.addObject("product", product);
        mav.addObject("productOptions", productMapper.toProductOptionClientDtoList(listProductOption));
        return mav;
    }

    @GetMapping("/api/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long id){
        Map<String, Object> result = new HashMap<>();
        List<ProductOption> listProductOption = productOptionService.getProductOptionsByProductId(id);
        ProductClientDto product = productMapper.toClientDto((Product) productService.getById(id));
        result.put("product", product);
        result.put("productOptions", productMapper.toProductOptionClientDtoList(listProductOption));
        return ResponseEntity.ok(result);
    }
}
