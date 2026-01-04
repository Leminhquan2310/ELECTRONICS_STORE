package com.electronics_store.controller.admin;

import com.electronics_store.dto.option.OptionDto;
import com.electronics_store.model.Option;
import com.electronics_store.model.ProductOption;
import com.electronics_store.service.OptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/product-options")
public class OptionController {

    @Autowired
    private OptionService optionService;

    @GetMapping("")
    public ModelAndView showProductOption(){
        ModelAndView mav = new ModelAndView("admin/product-option/list");
        List<OptionDto> options = optionService.findAll();
        mav.addObject("options", options);
        return mav;
    }
}
