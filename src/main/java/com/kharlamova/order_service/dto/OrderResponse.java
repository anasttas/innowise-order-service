package com.kharlamova.order_service.dto;

import com.kharlamova.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private UserDto user;

    private List<OrderItemResponse> orderItem;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
