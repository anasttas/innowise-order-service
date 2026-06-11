package com.kharlamova.order_service.controller;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.ItemDto;
import com.kharlamova.order_service.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Page<ItemDto>> getAllItems(Pageable pageable,
                                                      @RequestParam(required = false) String name,
                                                      @RequestParam(required = false) BigDecimal minPrice,
                                                      @RequestParam(required = false) BigDecimal  maxPrice
    ) {
        Page<ItemDto> itemDtos = itemService.getAllItems(name, minPrice, maxPrice, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(itemDtos);
    }

    @GetMapping("/{item_id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable("item_id") Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestBody @Valid ItemDto itemDto) {
        ItemDto createdItem = itemService.createItem(itemDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdItem);
    }

    @PatchMapping("/{item_id}")
    public ResponseEntity<ItemDto> updateItem(@PathVariable("item_id") Long itemId,
                                                     @RequestBody @Valid ItemDto itemDto
    ) {
        ItemDto updatedItem = itemService.updateItem(itemDto, itemId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedItem);
    }

    @DeleteMapping("/{item_id}")
    public ResponseEntity<AskDto> deleteItem(@PathVariable("item_id") Long itemId) {
        AskDto deletedUserDto = itemService.deleteItem(itemId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deletedUserDto);
    }
}
