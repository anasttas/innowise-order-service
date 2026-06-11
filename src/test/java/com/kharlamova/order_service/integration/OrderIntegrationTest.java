package com.kharlamova.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kharlamova.order_service.client.UserServiceClient;
import com.kharlamova.order_service.dto.*;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.entity.Order;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:8.2.1")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("user.service.url", () -> "http://localhost:8080");
    }

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE order_items RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE items RESTART IDENTITY CASCADE");
    }

    @Test
    void shouldCreateOrder() throws Exception {

        Item item = itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(1000))
                        .build()
        );

        when(userServiceClient.getUserByEmail("ivan@mail.com"))
                .thenReturn(UserDto.builder()
                        .id(1L)
                        .email("ivan@mail.com")
                        .build());

        when(userServiceClient.getUserById(1L))
                .thenReturn(UserDto.builder()
                        .id(1L)
                        .email("ivan@mail.com")
                        .build());

        OrderRequest request = OrderRequest.builder()
                .status(OrderStatus.NEW)
                .userEmail("ivan@mail.com")
                .orderItem(List.of(
                        OrderItemRequest.builder()
                                .itemId(item.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/shop/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalPrice").value(2000));

        assertThat(orderRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnOrderById() throws Exception {

        Order order = orderRepository.save(
                Order.builder()
                        .userId(1L)
                        .status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.valueOf(1000))
                        .build()
        );

        when(userServiceClient.getUserById(1L))
                .thenReturn(UserDto.builder()
                        .id(1L)
                        .email("ivan@mail.com")
                        .build());

        mockMvc.perform(get("/shop/orders/{order_id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void shouldReturnAllOrders() throws Exception {

        orderRepository.save(
                Order.builder()
                        .userId(1L)
                        .status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.TEN)
                        .build()
        );

        when(userServiceClient.getUserById(anyLong()))
                .thenReturn(UserDto.builder()
                        .id(1L)
                        .email("ivan@mail.com")
                        .build());

        mockMvc.perform(get("/shop/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldUpdateOrder() throws Exception {

        Item item = itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(2000))
                        .build()
        );

        Order order = orderRepository.save(
                Order.builder()
                        .userId(1L)
                        .status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.ZERO)
                        .build()
        );

        when(userServiceClient.getUserByEmail("ivan@mail.com"))
                .thenReturn(UserDto.builder()
                        .id(1L)
                        .email("ivan@mail.com")
                        .build());

        OrderRequest request = OrderRequest.builder()
                .status(OrderStatus.PAID)
                .userEmail("ivan@mail.com")
                .orderItem(List.of(
                        OrderItemRequest.builder()
                                .itemId(item.getId())
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(patch("/shop/orders/{order_id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldDeleteOrder() throws Exception {

        Order order = orderRepository.save(
                Order.builder()
                        .userId(1L)
                        .status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.ZERO)
                        .build()
        );

        mockMvc.perform(delete("/shop/orders/{order_id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());

        assertThat(orderRepository.findById(order.getId())).isEmpty();
    }
}
