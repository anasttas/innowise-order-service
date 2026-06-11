package com.kharlamova.order_service.service;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.ItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ItemService {
    ItemDto getItem(Long id);

    Page<ItemDto> getAllItems(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto, Long id);

    AskDto deleteItem(Long id);
}
