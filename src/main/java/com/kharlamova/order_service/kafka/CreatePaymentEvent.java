package com.kharlamova.order_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentEvent {
    private String eventType;

    private String paymentId;

    private Long orderId;

    private Long userId;

    private String paymentStatus;

    private BigDecimal paymentAmount;

    private Instant timestamp;
}
