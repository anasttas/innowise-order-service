package com.kharlamova.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderItemResponse {
    private Long id;

    private Long itemId;

    private int quantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
