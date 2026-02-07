package com.ocrs.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HealthController.class)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
class HealthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHealthEndpoint() {
        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("api-gateway")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void testRootEndpoint() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.service").isEqualTo("OCRS API Gateway")
                .jsonPath("$.version").isEqualTo("1.0.0")
                .jsonPath("$.status").isEqualTo("Running")
                .jsonPath("$.endpoints").exists()
                .jsonPath("$.endpoints.auth").isEqualTo("/api/auth/**")
                .jsonPath("$.endpoints.user").isEqualTo("/api/user/**")
                .jsonPath("$.endpoints.authority").isEqualTo("/api/authority/**")
                .jsonPath("$.endpoints.admin").isEqualTo("/api/admin/**")
                .jsonPath("$.endpoints.health").isEqualTo("/health");
    }

    @Test
    void testHealthEndpointReturnsTimestamp() {
        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.timestamp").isNotEmpty()
                .jsonPath("$.timestamp").value(timestamp -> {
                    assert timestamp instanceof String;
                    assert !((String) timestamp).isEmpty();
                });
    }

    @Test
    void testRootEndpointContainsAllEndpoints() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.endpoints.auth").exists()
                .jsonPath("$.endpoints.user").exists()
                .jsonPath("$.endpoints.authority").exists()
                .jsonPath("$.endpoints.admin").exists()
                .jsonPath("$.endpoints.health").exists();
    }

    @Test
    void testHealthEndpointMultipleCalls() {
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("UP")
                    .jsonPath("$.service").isEqualTo("api-gateway");
        }
    }

    @Test
    void testRootEndpointMultipleCalls() {
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.service").isEqualTo("OCRS API Gateway")
                    .jsonPath("$.status").isEqualTo("Running");
        }
    }
}