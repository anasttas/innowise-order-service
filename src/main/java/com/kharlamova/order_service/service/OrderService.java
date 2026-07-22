package com.kharlamova.order_service.service;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.OrderRequest;
import com.kharlamova.order_service.dto.OrderResponse;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.kafka.CreatePaymentEvent;
import com.kharlamova.order_service.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {
    OrderResponse getOrder(Long id, UserPrincipal principal);

    Page<OrderResponse> getAllOrdersByUserId(Long userId, Pageable pageable);

    Page<OrderResponse> getAllOrders(Pageable pageable, OrderStatus status,
                                    LocalDateTime startDate, LocalDateTime endDate);

    OrderResponse createOrder(OrderRequest orderDto, UserPrincipal principal);

    OrderResponse updateOrder(OrderRequest orderDto, Long id, UserPrincipal principal);

    AskDto deleteOrder(Long id);

    void handleCreatePaymentEvent(CreatePaymentEvent event);
}
