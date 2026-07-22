package com.kharlamova.order_service.service;

import com.kharlamova.order_service.dto.ItemDto;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.exception.ItemNotFoundException;
import com.kharlamova.order_service.mapper.ItemMapper;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.service.impl.ItemServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItem_shouldReturnItemDto() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Laptop");

        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Laptop");

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(itemMapper.makeItemDto(item))
                .thenReturn(dto);

        ItemDto result = itemService.getItem(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());

        verify(itemRepository).findById(1L);
        verify(itemMapper).makeItemDto(item);
    }

    @Test
    void getItem_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ItemNotFoundException.class,
                () -> itemService.getItem(1L)
        );

        verify(itemRepository).findById(1L);
    }

    @Test
    void createItem_shouldReturnItemDto() {
        ItemDto dto = new ItemDto();
        dto.setName("Laptop");
        dto.setPrice(BigDecimal.valueOf(1000));

        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1000));

        when(itemRepository.findByName("Laptop"))
                .thenReturn(Optional.empty());

        when(itemMapper.makeItem(dto))
                .thenReturn(item);

        when(itemMapper.makeItemDto(item))
                .thenReturn(dto);

        ItemDto result = itemService.createItem(dto);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());

        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_shouldUpdatePriceAndReturnDto() {
        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(100));

        ItemDto dto = new ItemDto();
        dto.setPrice(BigDecimal.valueOf(200));

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        when(itemMapper.makeItemDto(item))
                .thenReturn(dto);

        ItemDto result = itemService.updateItem(dto, 1L);

        assertEquals(
                BigDecimal.valueOf(200),
                result.getPrice()
        );

        verify(itemRepository).save(item);
    }

    @Test
    void deleteItem_shouldDeleteItem() {
        Item item = new Item();
        item.setId(1L);

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        itemService.deleteItem(1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void getAllItems_shouldReturnFilteredItems() {
        Item item = Item.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000))
                .build();

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000))
                .build();


        Page<Item> itemPage = new PageImpl<>(
                List.of(item)
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);

        when(itemMapper.makeItemDto(item)).thenReturn(dto);

        Page<ItemDto> result = itemService.getAllItems(
                "Lap",
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1500),
                pageable
        );

        assertNotNull(result);

        assertEquals(1, result.getContent().size());

        assertEquals(
                "Laptop",
                result.getContent()
                        .get(0)
                        .getName()
        );

        verify(itemRepository).findAll(any(Specification.class), eq(pageable));

        verify(itemMapper).makeItemDto(item);
    }
}
