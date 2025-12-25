package com.electronics_store.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ClientController {
    @GetMapping("")
    public String index(){
        return "client/index";
    }

    @GetMapping("/about")
    public String about(){
        return "client/about-us";
    }
}
