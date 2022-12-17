package com.uber.api.payment.service.dto;

import com.uber.api.common.api.constants.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private UUID id;
    private String requestId;
    private String customerMail;
    private BigDecimal price;
    private PaymentStatus status;
    private ZonedDateTime createdAt;


    public void validatePayment(List<String> failureMessage) {
        if (Objects.isNull(price) || !isGreaterThanZero(price)) {
            failureMessage.add("Payment price must be greater than zero");
        }
    }

    private boolean isGreaterThanZero(BigDecimal price) {
        return price.compareTo(BigDecimal.ZERO) > 0;
    }


    public void initializePayment() {
        id = UUID.randomUUID();
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }
}
