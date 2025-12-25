package com.electronics_store.controller;

import com.electronics_store.exception.RoleNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RoleNotFoundException.class)
    public String handleRoleNotFoundAtPostRegisterException(RoleNotFoundException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("message", "Register failed!!");
        redirectAttributes.addFlashAttribute("status", "warning");
        return "redirect:/auth/register";
    }
}
