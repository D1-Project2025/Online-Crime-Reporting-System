package com.ocrs.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HealthController Tests")
class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }

    @Test
    @DisplayName("Should return health status with UP status")
    void testHealthEndpoint() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("UP", response.getBody().get("status"));
                assertEquals("api-gateway", response.getBody().get("service"));
                assertTrue(response.getBody().containsKey("timestamp"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return health response with all required fields")
    void testHealthEndpointFields() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                Map<String, Object> body = response.getBody();
                assertNotNull(body);
                assertTrue(body.containsKey("status"));
                assertTrue(body.containsKey("service"));
                assertTrue(body.containsKey("timestamp"));
                assertEquals(3, body.size());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return timestamp in ISO format")
    void testHealthTimestampFormat() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                String timestamp = (String) response.getBody().get("timestamp");
                assertNotNull(timestamp);
                assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return root endpoint with service information")
    void testRootEndpoint() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertEquals("OCRS API Gateway", response.getBody().get("service"));
                assertEquals("1.0.0", response.getBody().get("version"));
                assertEquals("Running", response.getBody().get("status"));
                assertTrue(response.getBody().containsKey("endpoints"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return root endpoint with endpoints map")
    void testRootEndpointEndpointsMap() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, String> endpoints = (Map<String, String>) response.getBody().get("endpoints");
                assertNotNull(endpoints);
                assertEquals("/api/auth/**", endpoints.get("auth"));
                assertEquals("/api/user/**", endpoints.get("user"));
                assertEquals("/api/authority/**", endpoints.get("authority"));
                assertEquals("/api/admin/**", endpoints.get("admin"));
                assertEquals("/health", endpoints.get("health"));
                assertEquals(5, endpoints.size());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should return reactive Mono from health endpoint")
    void testHealthReturnsReactiveMono() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();
        assertNotNull(result);
        assertTrue(result instanceof Mono);
    }

    @Test
    @DisplayName("Should return reactive Mono from root endpoint")
    void testRootReturnsReactiveMono() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();
        assertNotNull(result);
        assertTrue(result instanceof Mono);
    }

    @Test
    @DisplayName("Should return consistent health status on multiple calls")
    void testHealthConsistency() {
        for (int i = 0; i < 10; i++) {
            Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("UP", response.getBody().get("status"));
                })
                .verifyComplete();
        }
    }

    @Test
    @DisplayName("Should return consistent root information on multiple calls")
    void testRootConsistency() {
        for (int i = 0; i < 10; i++) {
            Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();
            StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("OCRS API Gateway", response.getBody().get("service"));
                })
                .verifyComplete();
        }
    }

    @Test
    @DisplayName("Health endpoint should have non-null response body")
    void testHealthNonNullBody() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.getBody());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Root endpoint should have non-null response body")
    void testRootNonNullBody() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.getBody());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Health endpoint timestamp should be recent")
    void testHealthTimestampIsRecent() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                String timestamp = (String) response.getBody().get("timestamp");
                assertNotNull(timestamp);
                // Timestamp should contain current year
                assertTrue(timestamp.contains("202")); // Assuming we're in the 2020s
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Root endpoint should expose all service endpoints")
    void testRootExposesAllEndpoints() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                @SuppressWarnings("unchecked")
                Map<String, String> endpoints = (Map<String, String>) response.getBody().get("endpoints");
                assertTrue(endpoints.containsKey("auth"));
                assertTrue(endpoints.containsKey("user"));
                assertTrue(endpoints.containsKey("authority"));
                assertTrue(endpoints.containsKey("admin"));
                assertTrue(endpoints.containsKey("health"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Health status should always be UP")
    void testHealthStatusAlwaysUp() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals("UP", response.getBody().get("status"));
                assertNotEquals("DOWN", response.getBody().get("status"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Root endpoint status should always be Running")
    void testRootStatusAlwaysRunning() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals("Running", response.getBody().get("status"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should have correct service name in health response")
    void testHealthServiceName() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals("api-gateway", response.getBody().get("service"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should have correct service name in root response")
    void testRootServiceName() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals("OCRS API Gateway", response.getBody().get("service"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should have correct version in root response")
    void testRootVersion() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                assertEquals("1.0.0", response.getBody().get("version"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Health response should not contain null values")
    void testHealthNoNullValues() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.health();

        StepVerifier.create(result)
            .assertNext(response -> {
                Map<String, Object> body = response.getBody();
                body.values().forEach(value -> assertNotNull(value));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Root response should not contain null values")
    void testRootNoNullValues() {
        Mono<ResponseEntity<Map<String, Object>>> result = healthController.root();

        StepVerifier.create(result)
            .assertNext(response -> {
                Map<String, Object> body = response.getBody();
                assertNotNull(body.get("service"));
                assertNotNull(body.get("version"));
                assertNotNull(body.get("status"));
                assertNotNull(body.get("endpoints"));
            })
            .verifyComplete();
    }
}