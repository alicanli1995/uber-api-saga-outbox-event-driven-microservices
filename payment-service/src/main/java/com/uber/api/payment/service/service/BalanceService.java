package com.uber.api.payment.service.service;

import java.math.BigDecimal;

public interface BalanceService {
    BigDecimal getBalance(String mail);

}
