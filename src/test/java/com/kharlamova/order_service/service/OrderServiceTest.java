package com.kharlamova.order_service.service;

import com.kharlamova.order_service.client.UserServiceClient;
import com.kharlamova.order_service.dto.*;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.entity.Order;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.exception.OrderNotFoundException;
import com.kharlamova.order_service.mapper.OrderMapper;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.repository.OrderRepository;
import com.kharlamova.order_service.security.UserPrincipal;
import com.kharlamova.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrder_shouldReturnOrderResponse() {
        UserPrincipal principal = new UserPrincipal(10L, "USER");

        Order order = Order.builder()
                .id(1L)
                .userId(10L)
                .build();

        UserDto userDto = new UserDto();
        userDto.setId(10L);

        OrderResponse response = new OrderResponse();
        response.setUser(userDto);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(userServiceClient.getUserById(10L))
                .thenReturn(userDto);

        when(orderMapper.makeOrderDto(order, userDto))
                .thenReturn(response);

        OrderResponse result = orderService.getOrder(1L, principal);

        assertNotNull(result);

        verify(orderRepository).findById(1L);
        verify(userServiceClient).getUserById(10L);
    }

    @Test
    void getOrder_shouldThrowException_whenOrderNotFound() {
        UserPrincipal principal = new UserPrincipal(10L, "USER");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder(1L, principal)
        );

        verify(orderRepository).findById(1L);
    }

    @Test
    void createOrder_shouldCreateOrder() {
        UserPrincipal principal = new UserPrincipal(1L, "USER");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@mail.com");

        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(100));

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setUserEmail("test@mail.com");
        request.setStatus(OrderStatus.NEW);
        request.setOrderItem(List.of(itemRequest));

        Order savedOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .build();

        OrderResponse response = new OrderResponse();

        when(userServiceClient.getUserByEmail("test@mail.com"))
                .thenReturn(userDto);

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(userServiceClient.getUserById(1L))
                .thenReturn(userDto);

        when(orderMapper.makeOrderDto(savedOrder, userDto))
                .thenReturn(response);

        OrderResponse result = orderService.createOrder(request, principal);

        assertNotNull(result);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrder_shouldUpdateOrder() {
        UserPrincipal principal = new UserPrincipal(2L, "USER");

        Order order = Order.builder()
                .id(1L)
                .userId(1L)
                .build();

        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setEmail("new@mail.com");

        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.TEN);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(1L);
        itemRequest.setQuantity(3);

        OrderRequest request = new OrderRequest();
        request.setUserEmail("new@mail.com");
        request.setStatus(OrderStatus.PAID);
        request.setOrderItem(List.of(itemRequest));

        OrderResponse response = new OrderResponse();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(userServiceClient.getUserByEmail("new@mail.com"))
                .thenReturn(userDto);

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderMapper.makeOrderDto(order, userDto))
                .thenReturn(response);

        OrderResponse result = orderService.updateOrder(request, 1L, principal);

        assertNotNull(result);

        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_shouldDeleteOrder() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        AskDto result = orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void getAllOrders_shouldReturnOrdersPage() {
        Order order = Order.builder()
                .id(1L)
                .userId(10L)
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        UserDto userDto = UserDto.builder()
                .id(10L)
                .email("test@mail.com")
                .build();

        OrderResponse response = new OrderResponse();

        response.setUser(userDto);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order)
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(orderPage);

        when(userServiceClient.getUserById(10L)).thenReturn(userDto);

        when(orderMapper.makeOrderDto(order, userDto)).thenReturn(response);

        Page<OrderResponse> result =
                orderService.getAllOrders(
                        pageable,
                        OrderStatus.NEW,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now()
                );

        assertNotNull(result);

        assertEquals(1, result.getContent().size());

        assertEquals(
                userDto,
                result.getContent()
                        .get(0)
                        .getUser()
        );

        verify(orderRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(userServiceClient).getUserById(10L);

        verify(orderMapper).makeOrderDto(order, userDto);
    }

    @Test
    void getAllOrdersByUserId_shouldReturnOrdersPage() {
        Long userId = 10L;

        Order order = Order.builder()
                .id(1L)
                .userId(userId)
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.valueOf(200))
                .build();

        UserDto userDto = UserDto.builder()
                .id(userId)
                .email("test@mail.com")
                .build();

        OrderResponse response = new OrderResponse();
        response.setUser(userDto);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> orderPage = new PageImpl<>(
                List.of(order)
        );

        when(orderRepository.findOrderByUserId(userId, pageable))
                .thenReturn(orderPage);

        when(userServiceClient.getUserById(userId))
                .thenReturn(userDto);

        when(orderMapper.makeOrderDto(order, userDto))
                .thenReturn(response);

        Page<OrderResponse> result =
                orderService.getAllOrdersByUserId(
                        userId,
                        pageable
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.getContent().size()
        );

        assertEquals(
                userDto,
                result.getContent()
                        .get(0)
                        .getUser()
        );

        verify(orderRepository)
                .findOrderByUserId(
                        userId,
                        pageable
                );

        verify(userServiceClient).getUserById(userId);

        verify(orderMapper).makeOrderDto(order, userDto);
    }
}
