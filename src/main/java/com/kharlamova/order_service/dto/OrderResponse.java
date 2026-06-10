package com.kharlamova.order_service.dto;

import com.kharlamova.order_service.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;

    private OrderStatus status;

    private float totalPrice;

    private UserDto user;

    private List<OrderItemResponse> orderItem;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
