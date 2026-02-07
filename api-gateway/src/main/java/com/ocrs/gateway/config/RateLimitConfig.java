package com.ocrs.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration for API Gateway.
 * Uses IP-based rate limiting by default.
 */
@Configuration
public class RateLimitConfig {

        /**
         * Resolves a rate-limiting key from the client's IP address.
         *
         * @return the client's IP address to use as the rate-limit key, or "anonymous" if the IP cannot be determined
         */
        @Bean
        @Primary
        public KeyResolver ipKeyResolver() {
                return exchange -> {
                        String clientIp = exchange.getRequest().getRemoteAddress() != null
                                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                        : "anonymous";
                        return Mono.just(clientIp);
                };
        }

        /**
         * Creates a KeyResolver that uses the X-User-Id request header as the rate-limiting key.
         *
         * If the header is missing, the resolver uses the literal "anonymous".
         *
         * @return a KeyResolver that resolves the rate-limit key from the "X-User-Id" header or "anonymous" when absent
         */
        @Bean
        public KeyResolver userKeyResolver() {
                return exchange -> {
                        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
                        return Mono.just(userId != null ? userId : "anonymous");
                };
        }
}