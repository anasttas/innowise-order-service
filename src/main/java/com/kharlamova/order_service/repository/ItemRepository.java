package com.kharlamova.order_service.repository;

import com.kharlamova.order_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
