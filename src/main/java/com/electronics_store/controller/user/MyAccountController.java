package com.electronics_store.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user/my-accounts")
public class MyAccountController {
    @GetMapping("")
    public ModelAndView showMyAccount(){
        ModelAndView mav = new ModelAndView("client/my-account");
        return mav;
    }
}
