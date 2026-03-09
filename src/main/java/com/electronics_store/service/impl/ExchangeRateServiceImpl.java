package com.electronics_store.service.impl;

import com.electronics_store.service.ExchangeRateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
@Service
public class ExchangeRateServiceImpl  implements ExchangeRateService {
    // Tỉ giá dự phòng nếu API bị lỗi (Fallback)
    private static final BigDecimal DEFAULT_RATE_VND = new BigDecimal("25000");

    @Override
    @Cacheable(value = "exchangeRate", key = "'USD_VND'")
    public BigDecimal usdToVnd() {
        try {
            String url = "https://api.exchangerate-api.com/v4/latest/USD";

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            // Parse JSON để lấy field "VND"
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            double rate = root.path("rates").path("VND").asDouble();

            if (rate > 0) {
                return new BigDecimal(rate);
            }
        } catch (Exception e) {
            // Log lỗi: Không gọi được API, dùng tỉ giá mặc định
            System.err.println("Lỗi lấy tỉ giá: " + e.getMessage() + ". Sử dụng tỉ giá mặc định.");
        }

        return DEFAULT_RATE_VND;
    }

    @Override
    public BigDecimal eurToVnd() {
        return null;
    }

    @Override
    public BigDecimal jpyToVnd() {
        return null;
    }

    @Override
    public BigDecimal vndToUsd() {
        return null;
    }

    @Override
    public void clearUsdToVnd() {

    }
}
