package com.electronics_store.controller.client;

import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.service.CategoryService;
import com.electronics_store.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class ClientController {
    @Autowired
    private ProductService  productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("")
    public ModelAndView showHome(){
        ModelAndView mav = new ModelAndView("client/index");
        Pageable pageable = PageRequest.of(0, 4);
        mav.addObject("hotDealProducts", productService.getProductBySoldDesc(pageable).getContent());
        mav.addObject("laptopProducts", productService.getProductByCategoryName("Laptop", pageable).getContent());
        mav.addObject("tabletProducts", productService.getProductByCategoryName("Tablet", pageable).getContent());
        mav.addObject("smartphoneProducts", productService.getProductByCategoryName("Smartphone", pageable).getContent());
        mav.addObject("trendingProducts", productService.getProductBySoldDesc(pageable).getContent());
        mav.addObject("categoryRoots", categoryService.getListByLevelAndIsActiveTrue(1));
        return mav;
    }

    @GetMapping("/about")
    public String about(){
        return "client/about-us";
    }
}
