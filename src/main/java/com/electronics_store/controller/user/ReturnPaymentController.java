package com.electronics_store.controller.user;

import com.electronics_store.dto.vnpay.VnPayResponseDto;
import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import com.electronics_store.service.OrderService;
import com.electronics_store.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/user")
public class ReturnPaymentController {
    @Autowired
    private VnPayService vnPayService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/vnpay-return")
    public String showCongratulation(
            @ModelAttribute VnPayResponseDto vnPayResponseDto,
            HttpServletRequest request,
            Model model
    ) {
        int paymentStatus = vnPayService.orderReturn(request);

        if (paymentStatus == 1) {
            // thành công
            String orderId = vnPayResponseDto.getVnp_TxnRef(); // Mã đơn hàng
            String totalPrice = vnPayResponseDto.getVnp_Amount();
            String paymentTime = vnPayResponseDto.getVnp_PayDate();
            String transactionId = vnPayResponseDto.getVnp_TransactionNo();

            orderService.updateStatus(Long.valueOf(orderId), OrderStatus.PROCESSING, PaymentStatus.PAID);
            long amount = Long.parseLong(totalPrice) / 100;

            model.addAttribute("orderId", orderId);
            model.addAttribute("totalPrice", String.format("%,d", amount));
            model.addAttribute("paymentMethod", "VnPay");
            model.addAttribute("paymentTime", paymentTime);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("email", "customer@example.com");
            return "client/payment-result/order-success"; // Tên file HTML (đặt hàng thành công)
        } else if (paymentStatus == 0) {
            // thất bại
            model.addAttribute("message", "Thanh toán thất bại hoặc bị hủy.");
            return "client/payment-result/order-failed";
        } else {
            // sai checksum
            return "client/payment-result/order-invalid-signature";
        }
    }
}
