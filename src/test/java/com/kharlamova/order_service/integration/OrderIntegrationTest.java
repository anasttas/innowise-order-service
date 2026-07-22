package com.kharlamova.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kharlamova.order_service.dto.*;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.entity.Order;
import com.kharlamova.order_service.entity.OrderStatus;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.repository.OrderRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.kharlamova.order_service.security.UserPrincipal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("user.service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    private Authentication adminAuth;

    @BeforeEach
    void cleanDbAndStubs() {
        jdbcTemplate.execute("TRUNCATE TABLE order_items RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE items RESTART IDENTITY CASCADE");
        wireMockServer.resetAll();
        adminAuth = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(1L, "ADMIN"), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void verifyNoUnmatchedRequests() {
        assertThat(wireMockServer.findAllUnmatchedRequests()).isEmpty();
    }

    private RequestPostProcessor withPrincipal() {
        return request -> {
            request.setUserPrincipal(adminAuth);
            return request;
        };
    }

    private void stubUserByEmail(String email, long userId) {
        String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/users/email/" + encodedEmail))
                .willReturn(okJson("""
                        {"id": %d, "email": "%s", "name": "Ivan", "surname": "Ivanov", "birthDate": "1990-01-01", "active": true}
                        """.formatted(userId, email))));
    }

    private void stubUserById(long userId, String email) {
        wireMockServer.stubFor(WireMock.get(urlPathEqualTo("/users/" + userId))
                .willReturn(okJson("""
                        {"id": %d, "email": "%s", "name": "Ivan", "surname": "Ivanov", "birthDate": "1990-01-01", "active": true}
                        """.formatted(userId, email))));
    }

    private void stubUserByIdAnyTimes(long userId, String email) {
        stubUserById(userId, email);
    }

    @Test
    void shouldCreateOrder() throws Exception {
        Item item = itemRepository.save(
                Item.builder().name("Laptop").price(BigDecimal.valueOf(1000)).build());

        stubUserByEmail("ivan@mail.com", 1L);
        stubUserById(1L, "ivan@mail.com");

        OrderRequest request = OrderRequest.builder()
                .status(OrderStatus.NEW)
                .userEmail("ivan@mail.com")
                .orderItem(List.of(
                        OrderItemRequest.builder().itemId(item.getId()).quantity(2).build()))
                .build();

        mockMvc.perform(post("/shop/orders")
                        .with(withPrincipal())
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
                Order.builder().userId(1L).status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.valueOf(1000)).build());

        stubUserById(1L, "ivan@mail.com");

        mockMvc.perform(get("/shop/orders/{order_id}", order.getId())
                        .with(withPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Item item = itemRepository.save(
                Item.builder().name("Laptop").price(BigDecimal.valueOf(2000)).build());

        Order order = orderRepository.save(
                Order.builder().userId(1L).status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.ZERO).build());

        stubUserByEmail("ivan@mail.com", 1L);
        stubUserById(1L, "ivan@mail.com");

        OrderRequest request = OrderRequest.builder()
                .status(OrderStatus.PAID)
                .userEmail("ivan@mail.com")
                .orderItem(List.of(
                        OrderItemRequest.builder().itemId(item.getId()).quantity(1).build()))
                .build();

        mockMvc.perform(patch("/shop/orders/{order_id}", order.getId())
                        .with(withPrincipal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Order order = orderRepository.save(
                Order.builder().userId(1L).status(OrderStatus.NEW)
                        .totalPrice(BigDecimal.ZERO).build());

        mockMvc.perform(delete("/shop/orders/{order_id}", order.getId())
                        .with(withPrincipal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());

        assertThat(orderRepository.findById(order.getId())).isEmpty();
    }

    @Test
    void shouldFilterOrdersByStatus() throws Exception {
        orderRepository.save(Order.builder().userId(1L).status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN).build());
        orderRepository.save(Order.builder().userId(1L).status(OrderStatus.PAID)
                .totalPrice(BigDecimal.TEN).build());

        stubUserByIdAnyTimes(1L, "ivan@mail.com");

        mockMvc.perform(get("/shop/orders").with(withPrincipal()).param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PAID"));
    }

    @Test
    void shouldFilterOrdersByCreationDateRange() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO orders (user_id, status, total_price, created_at, deleted) VALUES (?, ?, ?, ?, ?)",
                1L, "NEW", BigDecimal.TEN, LocalDateTime.of(2026, 1, 1, 10, 0), false);
        jdbcTemplate.update(
                "INSERT INTO orders (user_id, status, total_price, created_at, deleted) VALUES (?, ?, ?, ?, ?)",
                1L, "NEW", BigDecimal.TEN, LocalDateTime.of(2026, 6, 1, 10, 0), false);

        stubUserByIdAnyTimes(1L, "ivan@mail.com");

        mockMvc.perform(get("/shop/orders")
                        .with(withPrincipal())
                        .param("startDate", "2026-05-01T00:00:00")
                        .param("endDate", "2026-07-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldPaginateOrders() throws Exception {
        for (int i = 0; i < 15; i++) {
            orderRepository.save(Order.builder().userId(1L).status(OrderStatus.NEW)
                    .totalPrice(BigDecimal.TEN).build());
        }

        stubUserByIdAnyTimes(1L, "ivan@mail.com");

        mockMvc.perform(get("/shop/orders").with(withPrincipal()).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15));

        mockMvc.perform(get("/shop/orders").with(withPrincipal()).param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    void shouldReturnOrdersByUserId() throws Exception {
        orderRepository.save(Order.builder().userId(1L).status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN).build());
        orderRepository.save(Order.builder().userId(2L).status(OrderStatus.NEW)
                .totalPrice(BigDecimal.TEN).build());

        stubUserByIdAnyTimes(1L, "ivan@mail.com");

        mockMvc.perform(get("/shop/orders/users").with(withPrincipal()).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].user.id").value(1));
    }
}
