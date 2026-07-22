package com.kharlamova.order_service.client;

import com.kharlamova.order_service.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    Authentication authentication = SecurityContextHolder
                            .getContext().getAuthentication();

                    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                        request.getHeaders()
                                .add("X-User-Id", principal.getUserId().toString());

                        request.getHeaders()
                                .add("X-User-Role", principal.getRole());
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}