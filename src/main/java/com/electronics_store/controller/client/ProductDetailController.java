package com.electronics_store.controller.client;

import com.electronics_store.dto.cart.AddToCartDto;
import com.electronics_store.dto.option.OptionDtoWithValues;
import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.Option;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductOption;
import com.electronics_store.service.OptionService;
import com.electronics_store.service.ProductOptionService;
import com.electronics_store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product-detail")
public class ProductDetailController {
    @Autowired
    private ProductOptionService productOptionService;
    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ModelAndView showProductDetail(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("client/product-detail");
        ProductClientDto productClientDto = productService.getProductForClient(id);
        Pageable pageable = PageRequest.of(0, 10);
        mav.addObject("productSameCategory", productService.getProductByCategoryName(productClientDto.getCategoryName(), pageable).getContent());
        mav.addObject("BASE_URL", "http://localhost:8080/");
        mav.addObject("addToCartDto", new AddToCartDto());
        mav.addObject("product", productClientDto);
        mav.addObject("options", productOptionService.getOptionsByProductId(id));
        return mav;
    }

}
