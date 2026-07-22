package com.kharlamova.order_service.service.impl;

import com.kharlamova.order_service.client.UserServiceClient;
import com.kharlamova.order_service.dto.*;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.entity.Order;
import com.kharlamova.order_service.entity.OrderItem;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.exception.ItemNotFoundException;
import com.kharlamova.order_service.exception.OrderNotFoundException;
import com.kharlamova.order_service.kafka.CreatePaymentEvent;
import com.kharlamova.order_service.mapper.OrderMapper;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.repository.OrderRepository;
import com.kharlamova.order_service.security.UserPrincipal;
import com.kharlamova.order_service.service.OrderService;
import com.kharlamova.order_service.specification.OrderSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final UserServiceClient userServiceClient;

    private final ItemRepository itemRepository;

    @Override
    public OrderResponse getOrder(Long id, UserPrincipal userPrincipal) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!userPrincipal.isAdmin()
                && !order.getUserId().equals(userPrincipal.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        UserDto userDto = userServiceClient.getUserById(order.getUserId());

        OrderResponse orderResponse = orderMapper.makeOrderDto(order, userDto);

        orderResponse.setUser(userDto);

        return orderResponse;
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable, OrderStatus status,
                                           LocalDateTime startDate, LocalDateTime endDate
    ) {
        Specification<Order> specification = Specification
                .where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.creationDateBetween(startDate, endDate));

        return orderRepository.findAll(specification, pageable)
                .map(order -> {
                    UserDto userDto = userServiceClient.getUserById(order.getUserId());
                    return orderMapper.makeOrderDto(order, userDto);
                });
    }

    @Override
    public Page<OrderResponse> getAllOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findOrderByUserId(userId, pageable)
                .map(order -> {
                    UserDto userDto = userServiceClient.getUserById(order.getUserId());
                    return orderMapper.makeOrderDto(order, userDto);
                });
    }

    @Override
    public OrderResponse createOrder(OrderRequest orderDto, UserPrincipal userPrincipal) {
        UserDto user = userServiceClient.getUserByEmail(orderDto.getUserEmail());

        if (!userPrincipal.isAdmin()
                && !user.getId().equals(userPrincipal.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        Order order = Order.builder()
                .userId(user.getId())
                .status(orderDto.getStatus())
                .totalPrice(BigDecimal.ZERO)
                .build();

        List<OrderItem> orderItems = convertOrderItems(orderDto, order);

        BigDecimal totalPrice = calculateTotalPrice(orderItems);

        order.setOrderItem(orderItems);
        order.setTotalPrice(totalPrice);

        Order saved = orderRepository.save(order);

        UserDto userDto = userServiceClient.getUserById(saved.getUserId());

        return orderMapper.makeOrderDto(saved, userDto);
    }

    @Transactional
    @Override
    public OrderResponse updateOrder(OrderRequest orderDto, Long id, UserPrincipal userPrincipal) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        UserDto userDto = userServiceClient.getUserByEmail(orderDto.getUserEmail());

        if (!userPrincipal.isAdmin()
                && !userDto.getId().equals(userPrincipal.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        order.setUserId(userDto.getId());
        order.setStatus(orderDto.getStatus());

        List<OrderItem> orderItems = convertOrderItems(orderDto, order);

        order.getOrderItem().clear();
        order.getOrderItem().addAll(orderItems);

        order.setTotalPrice(calculateTotalPrice(orderItems));

        Order saved = orderRepository.save(order);

        return orderMapper.makeOrderDto(saved, userDto);
    }

    @Transactional
    public AskDto deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        orderRepository.delete(order);

        return AskDto.makeDefault(true);
    }

    public void handleCreatePaymentEvent(CreatePaymentEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if ("SUCCESS".equals(event.getPaymentStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else if ("FAILED".equals(event.getPaymentStatus())) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    private List<OrderItem> convertOrderItems(OrderRequest orderDto, Order order) {
        return orderDto.getOrderItem().stream()
                .map(orderItemRequest -> {
                    Item item = itemRepository.findById(orderItemRequest.getItemId())
                            .orElseThrow(() -> new ItemNotFoundException("Item not found"));

                    return OrderItem.builder()
                            .item(item)
                            .quantity(orderItemRequest.getQuantity())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem ->
                        orderItem.getItem().getPrice()
                                .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
