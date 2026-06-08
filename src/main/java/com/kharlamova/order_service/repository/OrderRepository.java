package com.kharlamova.order_service.repository;

import com.kharlamova.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("SELECT c FROM Order c WHERE c.userId = :userId")
    List<Order> findOrdersByUserId(@Param("userId") Long userId);
}
