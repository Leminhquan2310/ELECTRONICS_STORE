package com.electronics_store.controller.user;

import com.electronics_store.dto.order.OrderStatusHistoryDto;
import com.electronics_store.dto.user.UpdateProfileRequest;
import com.electronics_store.model.Order;
import com.electronics_store.model.User;
import com.electronics_store.service.OrderService;
import com.electronics_store.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user/my-accounts")
public class MyAccountController {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public ModelAndView showMyAccount(Principal principal) {
        if (principal == null) {
            return new ModelAndView("redirect:/login");
        }
        Long id = userService.getCurrentUser().getId();
        ModelAndView mav = new ModelAndView("client/my-account");
        mav.addObject("updateProfile", new UpdateProfileRequest());
        mav.addObject("user", userService.getProfile(principal.getName()));
        mav.addObject("myOrders", orderService.getOrdersByUserId(id));
        return mav;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(
            @Valid @ModelAttribute("updateProfile") UpdateProfileRequest dto,
            BindingResult result,
            Principal principal,
            RedirectAttributes rd
    ) {
        if (principal == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mav = new ModelAndView("client/my-account");

        if (result.hasErrors()) {
            mav.addObject("userProfile", dto);
            mav.addObject("user", userService.getProfile(principal.getName()));
            return mav;
        }

        userService.updateUserProfile(principal.getName(), dto);
        rd.addFlashAttribute("message", "Update profile successfully!");
        rd.addFlashAttribute("status", "success");

        return new ModelAndView("redirect:/user/my-accounts");
    }

    @PostMapping("/update-avatar")
    @ResponseBody // Quan trọng: Báo cho Spring biết trả về dữ liệu, không phải view
    public ResponseEntity<?> updateAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            Principal principal
    ) {
        try {
            userService.updateAvatar(principal.getName(), avatar);
            // Trả về JSON báo thành công
            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "message", "Avatar updated successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Update failed"));
        }
    }

    @GetMapping("/orders/track/{orderId}")
    @ResponseBody
    public ResponseEntity<?> trackOrder(@PathVariable Long orderId, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Order order = orderService.getOrderById(orderId);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view this order's tracking.");
        }

        try {
            List<OrderStatusHistoryDto> trackingHistory = orderService.getOrderTrackingHistory(orderId);
            return ResponseEntity.ok(trackingHistory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving tracking history.");
        }
    }


    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("current_password") String currentPassword,
            @RequestParam("new_password") String newPassword,
            @RequestParam("confirm_password") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {

        // 1. Check confirm password
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("message", "Confirm password does not match");
            redirectAttributes.addFlashAttribute("status", "error");
            return "redirect:/user/my-accounts?tab=password";
        }

        try {
            userService.changePassword(
                    principal.getName(),
                    currentPassword,
                    newPassword
            );
            redirectAttributes.addFlashAttribute("status", "success");
            redirectAttributes.addFlashAttribute("message", "Password changed successfully");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            redirectAttributes.addFlashAttribute("status", "error");
        }

        return "redirect:/user/my-accounts?tab=password";
    }

}

