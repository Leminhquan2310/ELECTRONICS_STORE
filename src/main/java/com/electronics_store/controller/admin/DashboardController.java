package com.electronics_store.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin")
public class DashboardController {
    @GetMapping("")
    public ModelAndView dashboard(){
        return new ModelAndView( "admin/dashboard/index");
    }
}
