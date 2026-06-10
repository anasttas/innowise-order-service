package com.kharlamova.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AskDto {
    boolean answer;

    public static AskDto makeDefault(boolean answer) {
        return builder()
                .answer(answer)
                .build();
    }
}
