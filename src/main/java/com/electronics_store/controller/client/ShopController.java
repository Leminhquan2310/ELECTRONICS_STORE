package com.electronics_store.controller.client;

import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.Product;
import com.electronics_store.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductMapper productMapper;

    @GetMapping("")
    public ModelAndView showShop(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type
    ){
        ModelAndView mav = new ModelAndView("client/shop");

        // Nếu có keyword hoặc type, thực hiện tìm kiếm/lọc
        if (keyword != null || type != null) {
            // Logic tìm kiếm
//            List<Product> products = productService.search(keyword, type);
//            mav.addObject("products", products);
//            mav.addObject("keyword", keyword);
//            mav.addObject("type", type);
        } else {
            // Hiển thị tất cả sản phẩm
            Page<Product> productPage = productService.getProductByCreatedAt(pageable);
            Page<ProductClientDto> page = productPage.map(productMapper::toClientDto);
            mav.addObject("page", page);
        }

        return mav;
    }
}
