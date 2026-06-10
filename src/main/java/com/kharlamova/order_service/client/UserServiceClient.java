package com.kharlamova.order_service.client;

import com.kharlamova.order_service.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class UserServiceClient {
    private final RestClient restClient;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "fallbackGetUserByEmail"
    )
    public UserDto getUserByEmail(String email) {
        return restClient.get()
                .uri(userServiceUrl + "/users/email/{email}", email)
                .retrieve()
                .body(UserDto.class);
    }

    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "fallbackGetUserById"
    )
    public UserDto getUserById(Long id) {
        return restClient.get()
                .uri(userServiceUrl + "/users/{id}", id)
                .retrieve()
                .body(UserDto.class);
    }

    public UserDto fallbackGetUserByEmail(String email, Exception ex) {
        throw new RuntimeException("User service unavailable");
    }

    public UserDto fallbackGetUserById(Long id, Exception ex) {
        throw new RuntimeException("User service unavailable", ex);
    }
}
