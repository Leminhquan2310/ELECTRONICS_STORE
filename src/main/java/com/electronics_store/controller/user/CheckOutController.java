package com.electronics_store.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user/checkouts")
public class CheckOutController {

    @GetMapping("")
    public ModelAndView showCheckout(){
        return new ModelAndView("client/checkout");
    }
}
