package com.electronics_store.controller.client;

import com.electronics_store.dto.category.CategoryDto;
import com.electronics_store.dto.option.FilterOptionDto;
import com.electronics_store.dto.product.ProductClientDto;
import com.electronics_store.model.Category;
import com.electronics_store.repository.CategoryRepository;
import com.electronics_store.service.CategoryService;
import com.electronics_store.service.OptionService;
import com.electronics_store.service.ProductService;
import com.electronics_store.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ShopService shopService;


    @GetMapping
    public String shopPage(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> optionValueIds, // Nhận List ID thay vì String
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "trending") String sortBy
    ) {
        // 1. Get filtered products
        Page<ProductClientDto> products = shopService.filterProducts(keyword, categoryIds, optionValueIds, page, size, sortBy);

        // 2. Get Sidebar Data
        List<CategoryDto> categories = categoryService.getListRootForClient();
        List<FilterOptionDto> filters = shopService.getFilterOptions();

        // 3. Add to Model
        model.addAttribute("productPage", products);
        model.addAttribute("categories", categories);
        model.addAttribute("filters", filters);

        // 4. Retain Selection State (Để checkbox vẫn được tích sau khi reload)
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategories", categoryIds != null ? categoryIds : new ArrayList<>());
        model.addAttribute("selectedOptionValues", optionValueIds != null ? optionValueIds : new ArrayList<>());
        model.addAttribute("sortBy", sortBy);

        return "client/shop";
    }
}
