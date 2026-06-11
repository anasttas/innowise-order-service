package com.kharlamova.order_service.specification;

import com.kharlamova.order_service.entity.Item;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ItemSpecification {
    public static Specification<Item> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Item> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null || maxPrice== null) {
                return cb.conjunction();
            }
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }
}
