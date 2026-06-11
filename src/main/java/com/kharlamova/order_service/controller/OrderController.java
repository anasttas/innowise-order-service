package com.kharlamova.order_service.controller;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.OrderRequest;
import com.kharlamova.order_service.dto.OrderResponse;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{order_id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("order_id") Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable,
                                  @RequestParam(required = false) OrderStatus status,
                                  @RequestParam(required = false) LocalDateTime startDate,
                                  @RequestParam(required = false) LocalDateTime endDate
    ) {
        Page<OrderResponse> orderResponses = orderService.getAllOrders(pageable, status, startDate, endDate);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderResponses);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> addOrder(@RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse createdOrder = orderService.createOrder(orderRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOrder);
    }

    @PatchMapping("/{order_id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable("order_id") Long orderId,
                                              @RequestBody @Valid OrderRequest orderRequest
    ) {
        OrderResponse updatedOrder = orderService.updateOrder(orderRequest, orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedOrder);
    }

    @DeleteMapping("/{order_id}")
    public ResponseEntity<AskDto> deleteOrder(@PathVariable("order_id") Long orderId) {
        AskDto deletedUserDto = orderService.deleteOrder(orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deletedUserDto);
    }
}
