package com.ocrs.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Health check controller for the API Gateway.
 */
@RestController
public class HealthController {

        /**
         * Provide a health check response for the API gateway.
         *
         * The response body contains "status" set to "UP", "service" set to "api-gateway",
         * and "timestamp" set to the current time as an ISO-8601 string.
         *
         * @return ResponseEntity with HTTP 200 OK and a Map containing the health information
         */
        @GetMapping("/health")
        public Mono<ResponseEntity<Map<String, Object>>> health() {
                return Mono.just(ResponseEntity.ok(Map.of(
                                "status", "UP",
                                "service", "api-gateway",
                                "timestamp", Instant.now().toString())));
        }

        /**
         * Exposes the API root metadata and a listing of available gateway endpoints.
         *
         * @return a ResponseEntity whose body is a Map with keys:
         *         "service" (service name), "version" (service version), "status" (current status),
         *         and "endpoints" (a nested Map of endpoint paths for auth, user, authority, admin, and health).
         */
        @GetMapping("/")
        public Mono<ResponseEntity<Map<String, Object>>> root() {
                return Mono.just(ResponseEntity.ok(Map.of(
                                "service", "OCRS API Gateway",
                                "version", "1.0.0",
                                "status", "Running",
                                "endpoints", Map.of(
                                                "auth", "/api/auth/**",
                                                "user", "/api/user/**",
                                                "authority", "/api/authority/**",
                                                "admin", "/api/admin/**",
                                                "health", "/health"))));
        }
}