package com.kharlamova.order_service.service;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.OrderRequest;
import com.kharlamova.order_service.dto.OrderResponse;
import com.kharlamova.order_service.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {
    OrderResponse getOrder(Long id);

    Page<OrderResponse> getAllOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable, OrderStatus status,
                                    LocalDateTime startDate, LocalDateTime endDate);

    OrderResponse createOrder(OrderRequest orderDto);

    public OrderResponse updateOrder(OrderRequest orderDto, Long id);

    AskDto deleteOrder(Long id);
}
