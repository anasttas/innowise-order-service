package com.kharlamova.order_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemRequest {
    private Long itemId;

    private int quantity;
}
