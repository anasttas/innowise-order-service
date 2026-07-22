package com.kharlamova.order_service.service.impl;

import com.kharlamova.order_service.dto.AskDto;
import com.kharlamova.order_service.dto.ItemDto;
import com.kharlamova.order_service.entity.Item;
import com.kharlamova.order_service.exception.ItemAlreadyExistError;
import com.kharlamova.order_service.exception.ItemNotFoundException;
import com.kharlamova.order_service.mapper.ItemMapper;
import com.kharlamova.order_service.repository.ItemRepository;
import com.kharlamova.order_service.service.ItemService;
import com.kharlamova.order_service.specification.ItemSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Override
    public ItemDto getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        return itemMapper.makeItemDto(item);
    }

    @Override
    public Page<ItemDto> getAllItems(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Item> specification = Specification
                .where(ItemSpecification.hasNameLike(name))
                .and(ItemSpecification.priceBetween(minPrice, maxPrice));

        return itemRepository.findAll(specification, pageable)
                .map(itemMapper::makeItemDto);
    }

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        itemRepository.findByName(itemDto.getName())
                .ifPresent(foundItem -> {
                    throw new ItemAlreadyExistError("Item already exists");
                });

        Item item = itemMapper.makeItem(itemDto);

        itemRepository.save(item);

        return itemMapper.makeItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto, Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        item.setPrice(itemDto.getPrice());

        itemRepository.save(item);

        return itemMapper.makeItemDto(item);
    }

    @Override
    @Transactional
    public AskDto deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        itemRepository.delete(item);

        return AskDto.makeDefault(true);
    }
}
