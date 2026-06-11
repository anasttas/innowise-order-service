package com.kharlamova.order_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kharlamova.order_service.dto.ItemDto;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.repository.ItemRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("USER_SERVICE_URL", () -> "http://localhost:8080");
    }

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE items RESTART IDENTITY CASCADE");
    }

    @Test
    void shouldCreateItemAndSaveToDatabase() throws Exception {

        ItemDto itemDto = ItemDto.builder()
                .name("Laptop")
                .price(BigDecimal.valueOf(1500))
                .build();

        mockMvc.perform(post("/shop/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));

        assertThat(itemRepository.findAll()).hasSize(1);

        Item savedItem = itemRepository.findAll().get(0);

        assertThat(savedItem.getName()).isEqualTo("Laptop");
        assertThat(savedItem.getPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void shouldReturnItemById() throws Exception {

        Item item = itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(1500))
                        .build()
        );

        mockMvc.perform(get("/shop/items/{item_id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void shouldReturnAllItems() throws Exception {

        itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(1500))
                        .build()
        );

        mockMvc.perform(get("/shop/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldUpdateItem() throws Exception {

        Item item = itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(1500))
                        .build()
        );

        ItemDto updateDto = ItemDto.builder()
                .name("Laptop")
                .price(BigDecimal.valueOf(2000))
                .build();

        mockMvc.perform(patch("/shop/items/{order_id}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(2000));

        Item updated = itemRepository.findById(item.getId()).orElseThrow();

        assertThat(updated.getPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void shouldDeleteItem() throws Exception {

        Item item = itemRepository.save(
                Item.builder()
                        .name("Laptop")
                        .price(BigDecimal.valueOf(1500))
                        .build()
        );

        mockMvc.perform(delete("/shop/items/{order_id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());

        assertThat(itemRepository.findById(item.getId())).isEmpty();
    }
}
