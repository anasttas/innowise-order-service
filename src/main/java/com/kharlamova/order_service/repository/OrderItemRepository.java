package com.kharlamova.order_service.repository;

import com.kharlamova.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query(value = "SELECT * FROM order_items WHERE order_id = :order_id", nativeQuery = true)
    List<OrderItem> findOrderItemByOrderId(@Param("order_id") Long orderId);

    @Query("SELECT c FROM OrderItem c WHERE c.item.id = :itemId")
    List<OrderItem> findOrderItemByItemId(@Param("itemId") Long itemId);
}
