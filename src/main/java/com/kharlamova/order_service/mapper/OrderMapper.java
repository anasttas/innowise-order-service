package com.kharlamova.order_service.mapper;

import com.kharlamova.order_service.dto.OrderItemResponse;
import com.kharlamova.order_service.dto.OrderRequest;
import com.kharlamova.order_service.dto.OrderResponse;
import com.kharlamova.order_service.dto.UserDto;
import com.kharlamova.order_service.entity.Order;
import com.kharlamova.order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "orderItem", source = "order.orderItem")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "updatedAt", source = "order.updatedAt")
    OrderResponse makeOrderDto(Order order, UserDto user);

    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponse makeOrderItemDto(OrderItem orderItem);
}
