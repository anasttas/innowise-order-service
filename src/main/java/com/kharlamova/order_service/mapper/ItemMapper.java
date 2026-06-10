package com.kharlamova.order_service.mapper;

import com.kharlamova.order_service.dto.ItemDto;
import com.kharlamova.order_service.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto makeItemDto(Item item);

    Item makeItem(ItemDto itemDto);
}
