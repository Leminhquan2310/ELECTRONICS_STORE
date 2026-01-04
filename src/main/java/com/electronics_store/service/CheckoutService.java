package com.electronics_store.service;

import com.electronics_store.dto.checkout.CheckoutRequestDto;
import com.electronics_store.model.Order;
import com.electronics_store.model.User;

public interface CheckoutService {
    Order placeOrder(CheckoutRequestDto request, User currentUser);
}
