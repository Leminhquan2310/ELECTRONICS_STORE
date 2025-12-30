package com.electronics_store.controller.admin;

import com.electronics_store.dto.product_variant.ProductVariantAdminDto;
import com.electronics_store.dto.product_variant.ProductVariantDtoUpdate;
import com.electronics_store.mapper.ProductVariantMapper;
import com.electronics_store.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/product-variants")
public class ProductVariantController {
    @Value("${app.base-url}")
    private String baseUrl;
    @Autowired
    private ProductVariantService productVariantService;
    @Autowired
    private ProductVariantMapper productVariantMapper;

    @GetMapping("")
    public ModelAndView showListProductVariant(){
        ModelAndView mav = new ModelAndView("admin/product-variant/list");
        List<ProductVariantAdminDto> productVariantAdminDto = productVariantMapper.listProductVariantToAdminDto(productVariantService.getAll());
        mav.addObject("productVariantAdminDto", productVariantAdminDto);
        mav.addObject("BASE_URL", baseUrl);
        mav.addObject("productVariantDtoUpdate", new ProductVariantDtoUpdate());
        return mav;
    }

    @PostMapping("/update")
    public String updateProductVariant(ProductVariantDtoUpdate productVariantDtoUpdate, RedirectAttributes redirectAttributes){
        productVariantService.update(productVariantDtoUpdate);
        redirectAttributes.addFlashAttribute("status", "success");
        redirectAttributes.addFlashAttribute("message", "Update product variant successfully!");
        return "redirect:/admin/product-variants";
    }
}
