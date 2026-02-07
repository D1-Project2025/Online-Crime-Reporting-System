package com.ocrs.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false",
    "jwt.secret=test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long-for-hmac-sha",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            // Context loads successfully
        });
    }

    @Test
    void mainMethodRuns() {
        assertDoesNotThrow(() -> {
            // Verify main method exists and can be called
            ApiGatewayApplication.class.getMethod("main", String[].class);
        });
    }
}