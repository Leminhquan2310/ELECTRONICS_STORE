package com.electronics_store.controller.admin;

import com.electronics_store.dto.category.CategoryDtoCreate;
import com.electronics_store.dto.product.ProductDtoCreate;
import com.electronics_store.dto.product.ProductDtoUpdate;
import com.electronics_store.mapper.ProductMapper;
import com.electronics_store.model.Product;
import com.electronics_store.model.ProductOptionValue;
import com.electronics_store.service.CategoryService;
import com.electronics_store.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/products")
public class ProductController {
    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductMapper productMapper;

    @GetMapping("")
    public ModelAndView showListAllProduct() {
        ModelAndView mav = new ModelAndView("admin/product/list");
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("products", productService.getAll());
        return mav;
    }

    @GetMapping(value = "/create")
    public ModelAndView showCreateProduct() {
        ModelAndView mav = new ModelAndView("admin/product/create");
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("productDtoCreate", new ProductDtoCreate());
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        return mav;
    }

    @GetMapping(value = "/update/{id}")
    public ModelAndView showUpdateProduct(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("admin/product/update");
        ProductDtoUpdate productDtoUpdate = productMapper.toDtoUpdate((Product) productService.getById(id));
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("productDtoUpdate", productDtoUpdate);
        mav.addObject("images", productService.getImagesByProductId(id));
        mav.addObject("productOptions", productMapper.toMapOptionValue(productService.getProductOptionValuesByProductId(id)));
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        return mav;
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelAndView createProduct(@ModelAttribute @Valid ProductDtoCreate productDtoCreate, BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView("admin/product/create");
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        if (bindingResult.hasErrors()) {
            return mav;
        }
        productService.create(productDtoCreate);
        mav.addObject("product", new ProductDtoCreate());
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        mav.addObject("status", "success");
        mav.addObject("message", "Create product successfully!");
        return mav;
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelAndView updateProduct(@ModelAttribute @Valid ProductDtoUpdate productDtoUpdate, BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView("admin/product/update");
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("images", productService.getImagesByProductId(productDtoUpdate.getId()));
        mav.addObject("productOptions", productMapper.toMapOptionValue(productService.getProductOptionValuesByProductId(productDtoUpdate.getId())));
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        if (bindingResult.hasErrors()) {
            return mav;
        }
        productService.update(productDtoUpdate);
        mav.addObject("images", productService.getImagesByProductId(productDtoUpdate.getId()));
        mav.addObject("productOptions", productMapper.toMapOptionValue(productService.getProductOptionValuesByProductId(productDtoUpdate.getId())));
        mav.addObject("categories", categoryService.getListByIsLeafTrue());
        mav.addObject("status", "success");
        mav.addObject("message", "Create product successfully!");
        return mav;
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean isSuccess = productService.delete(id);
        redirectAttributes.addFlashAttribute("status", isSuccess ? "success" : "error");
        redirectAttributes.addFlashAttribute("message", isSuccess ? "Delete product successfully!" : "Delete product failed!");
        return "redirect:/admin/products";
    }
}
