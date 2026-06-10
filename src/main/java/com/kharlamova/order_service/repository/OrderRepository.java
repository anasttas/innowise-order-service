package com.kharlamova.order_service.repository;

import com.kharlamova.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    @Query("SELECT c FROM Order c WHERE c.userId = :userId")
    Page<Order> findOrderByUserId(Long userId, Pageable pageable);
}
