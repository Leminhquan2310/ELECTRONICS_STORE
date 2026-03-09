package com.electronics_store.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface VnPayService {
    String createPaymentUrl(HttpServletRequest request, long amount, String bankCode, long orderInfo) throws UnsupportedEncodingException;

    int orderReturn(HttpServletRequest request);
}
