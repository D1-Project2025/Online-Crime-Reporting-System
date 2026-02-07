package com.ocrs.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
@DisplayName("ApiGatewayApplication Tests")
class ApiGatewayApplicationTest {

    @Test
    @DisplayName("Should load Spring application context successfully")
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads without errors
        // If this test passes, it means all beans are correctly configured
    }

    @Test
    @DisplayName("Should start application with main method")
    void testMainMethod() {
        // Test that main method can be invoked without exceptions
        // In a real scenario, this would start the application
        String[] args = {};
        // ApiGatewayApplication.main(args); // Commented out to avoid actually starting the app in tests
        // Instead, we verify the class structure is correct
        assertMainMethodExists();
    }

    private void assertMainMethodExists() {
        try {
            ApiGatewayApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist", e);
        }
    }
}