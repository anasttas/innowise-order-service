package com.kharlamova.order_service.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserDto {
    private Long id;

    private String email;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private Boolean active;
}
