package com.electronics_store.model;

import com.electronics_store.model.enums.OrderStatus;
import com.electronics_store.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    // Liên kết với User (nếu đã đăng nhập hoặc tạo mới tài khoản)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thông tin Billing (Bắt buộc)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "billing_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "billing_last_name")),
            @AttributeOverride(name = "companyName", column = @Column(name = "billing_company_name")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "billing_street_address")),
            @AttributeOverride(name = "apartment", column = @Column(name = "billing_apartment")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip_code")),
            @AttributeOverride(name = "email", column = @Column(name = "billing_email")),
            @AttributeOverride(name = "phone", column = @Column(name = "billing_phone"))
    })
    private AddressInfo billingAddress;

    // Thông tin Shipping (Đã sửa hoàn toàn các cột trỏ về shipping_...)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "shipping_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "shipping_last_name")),
            @AttributeOverride(name = "companyName", column = @Column(name = "shipping_company_name")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country")),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "shipping_street_address")),
            @AttributeOverride(name = "apartment", column = @Column(name = "shipping_apartment")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
            @AttributeOverride(name = "email", column = @Column(name = "shipping_email")),
            @AttributeOverride(name = "phone", column = @Column(name = "shipping_phone"))
    })
    private AddressInfo shippingAddress;

    private String orderNotes;

    // Payment
    private String paymentMethod; // "Direct Bank Transfer", "Paypal", etc.
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // PENDING, PAID

    // Coupon
    private String couponCode;
    private BigDecimal discountAmount;

    // Totals
    private BigDecimal subTotal;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, PROCESSING, COMPLETED, CANCELLED

    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        // Mặc định 5 ngày sau khi tạo đơn
        if (this.estimatedDelivery == null) {
            this.estimatedDelivery = this.createdAt.plusDays(5);
        }

        if (this.orderDate == null) {
            this.orderDate = this.createdAt;
        }
    }
}