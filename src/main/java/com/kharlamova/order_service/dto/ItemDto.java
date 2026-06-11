package com.kharlamova.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Name should not be blank")
    @Size(min = 1, max = 50, message = "Name should be between {min} and {max} characters")
    private String name;

    @Positive(message = "Price should be positive")
    private BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
