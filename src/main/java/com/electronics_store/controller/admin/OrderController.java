package com.electronics_store.controller.admin;

import com.electronics_store.model.Order;
import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import com.electronics_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        return "admin/order/list";
    }

    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "admin/order/detail";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long orderId,
                               @RequestParam OrderStatus status,
                               @RequestParam PaymentStatus paymentStatus,
                               RedirectAttributes ra) {
        try {
            orderService.updateStatus(orderId, status, paymentStatus);
            ra.addFlashAttribute("message", "Cập nhật đơn hàng thành công!");
            ra.addFlashAttribute("status", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            ra.addFlashAttribute("status", "error");
        }
        return "redirect:/admin/orders";
    }
}