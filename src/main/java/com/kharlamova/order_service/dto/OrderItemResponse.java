package com.kharlamova.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;

    private Long itemId;

    private int quantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
