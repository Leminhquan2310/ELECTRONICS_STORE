package com.electronics_store.dto.checkout;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CheckoutRequestDto {
    // --- Billing Details (Thông tin thanh toán) ---
    private String billingCountry;
    private String billingFirstName;
    private String billingLastName;
    private String billingCompany;
    private String billingAddress;
    private String billingApartment;
    private String billingCity;
    private String billingState;
    private String billingZip;
    private String billingEmail; // Có thể pre-fill từ user đã login nhưng vẫn cho phép sửa
    private String billingPhone;

    // BankCode for VnPay
    private String bankCode;


    // Ship to different address Checkbox
    private boolean shipToDifferentAddress;

    // --- Shipping Details (Chỉ dùng nếu checkbox trên = true) ---
    private String shippingCountry;
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingCompany;
    private String shippingAddress;
    private String shippingApartment;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingEmail;
    private String shippingPhone;

    // Order Info
    private String orderNotes;
    private String couponCode;

    // Payment Method (radio button value)
    private String paymentMethod;
}
