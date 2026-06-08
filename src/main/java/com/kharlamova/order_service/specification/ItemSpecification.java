package com.kharlamova.order_service.specification;

import com.kharlamova.order_service.entity.Item;
import org.springframework.data.jpa.domain.Specification;

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

    public static Specification<Item> priceBetween(float minPrice, float maxPrice) {
        return (root, query, cb) ->
                cb.between(root.get("price"), minPrice, maxPrice);
    }
}
