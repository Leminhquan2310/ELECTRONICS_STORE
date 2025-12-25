package com.electronics_store.controller;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.mapper.UserMapper;
import com.electronics_store.model.Category;
import com.electronics_store.model.User;
import com.electronics_store.service.CategoryService;
import com.electronics_store.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @PostMapping("")
    public ModelAndView register(
            @Validated @ModelAttribute("userDtoRegister") UserDtoRegister userDtoRegister,
            BindingResult bindingResult
    ) {
        ModelAndView mav = new ModelAndView("auth/register-form");
        if (!bindingResult.hasErrors()) {
            userDetailsService.checkUniqueUsernameRegister(userDtoRegister, bindingResult);
        }
        if (bindingResult.hasErrors()) {
            return mav;
        }
        userDetailsService.create(userDtoRegister);
        mav.addObject("userDtoRegister", new UserDtoRegister());
        mav.addObject("message", "Register successfully!!");
        mav.addObject("status", "success");

        return mav;
    }

    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("auth/login-form");
    }

    @GetMapping("/register")
    public ModelAndView showRegister() {
        ModelAndView mav = new ModelAndView("auth/register-form");
        mav.addObject("userDtoRegister", new UserDtoRegister());
        return mav;
    }
}
