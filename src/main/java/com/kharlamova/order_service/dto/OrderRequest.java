package com.kharlamova.order_service.dto;

import com.kharlamova.order_service.entity.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderRequest {
    private Long id;

    @NotNull(message = "Status should not be blank")
    private OrderStatus status;

    @NotNull(message = "User email id can not be null")
    private String userEmail;

    @NotEmpty(message = "List of values can not be empty")
    private List<OrderItemRequest> orderItem;
}
