package com.electronics_store.service.impl;

import com.electronics_store.dto.checkout.CheckoutRequestDto;
import com.electronics_store.model.*;
import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import com.electronics_store.repository.CouponRepository;
import com.electronics_store.repository.OrderRepository;
import com.electronics_store.service.CartService;
import com.electronics_store.service.CheckoutService;
import com.electronics_store.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartService cartService; // Service lấy giỏ hàng hiện tại
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponRepository couponRepository;

    @Transactional
    public Order placeOrder(CheckoutRequestDto request, User currentUser) {
        // 1. Kiểm tra bảo mật
        if (currentUser == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập.");
        }

        // 2. Lấy danh sách sản phẩm từ giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống.");
        }

        // 3. Khởi tạo đối tượng Order
        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderNotes(request.getOrderNotes());

        // 4. Thiết lập địa chỉ (Billing & Shipping)
        AddressInfo billing = mapBillingAddress(request);
        order.setBillingAddress(billing);

        if (request.isShipToDifferentAddress()) {
            order.setShippingAddress(mapShippingAddress(request));
        } else {
            order.setShippingAddress(billing);
        }

        // 5. Xử lý danh sách sản phẩm và tính SubTotal
        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            // Lấy giá từ Variant (Biến thể sản phẩm)
            BigDecimal unitPrice = item.getVariant().getPriceAdjustment();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(item.getProduct())
                    .productVariant(item.getVariant())
                    .quantity(item.getQuantity())
                    .price(unitPrice)
                    .build();

            orderItems.add(orderItem);

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(lineTotal);
        }
        order.setOrderItems(orderItems);
        order.setSubTotal(subTotal);

        // 6. Xử lý Coupon và tính Discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            try {
                // Validate coupon lần cuối tại server
                Coupon coupon = couponService.validateAndGetCoupon(request.getCouponCode(), subTotal);
                discountAmount = couponService.calculateDiscount(coupon, subTotal);

                // Cập nhật thông tin mã đã dùng
                order.setCouponCode(coupon.getCode());

                // Tăng số lượt đã sử dụng của coupon
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                couponRepository.save(coupon);
            } catch (Exception e) {
                // Tùy chọn: Có thể throw lỗi hoặc tiếp tục thanh toán không giảm giá
                throw new RuntimeException("Lỗi áp dụng mã giảm giá: " + e.getMessage());
            }
        }

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(subTotal.subtract(discountAmount));

        // 7. Lưu đơn hàng và Xóa giỏ hàng
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(currentUser);

        return savedOrder;
    }

// --- Các hàm Helper để code sạch hơn ---

    private AddressInfo mapBillingAddress(CheckoutRequestDto dto) {
        return AddressInfo.builder()
                .firstName(dto.getBillingFirstName())
                .lastName(dto.getBillingLastName())
                .companyName(dto.getBillingCompany())
                .country(dto.getBillingCountry())
                .streetAddress(dto.getBillingAddress())
                .apartment(dto.getBillingApartment())
                .city(dto.getBillingCity())
                .state(dto.getBillingState())
                .zipCode(dto.getBillingZip())
                .email(dto.getBillingEmail())
                .phone(dto.getBillingPhone())
                .build();
    }

    private AddressInfo mapShippingAddress(CheckoutRequestDto dto) {
        return AddressInfo.builder()
                .firstName(dto.getShippingFirstName())
                .lastName(dto.getShippingLastName())
                .companyName(dto.getShippingCompany())
                .country(dto.getShippingCountry())
                .streetAddress(dto.getShippingAddress())
                .apartment(dto.getShippingApartment())
                .city(dto.getShippingCity())
                .state(dto.getShippingState())
                .zipCode(dto.getShippingZip())
                .email(dto.getShippingEmail())
                .phone(dto.getShippingPhone())
                .build();
    }
}
