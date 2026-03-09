package com.electronics_store.service;

import java.math.BigDecimal;

public interface ExchangeRateService  {
    BigDecimal usdToVnd();

    BigDecimal eurToVnd();

    BigDecimal jpyToVnd();

    BigDecimal vndToUsd();

    void clearUsdToVnd();
}
