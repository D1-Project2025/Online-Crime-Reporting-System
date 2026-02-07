package com.ocrs.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorsWebFilterTest {

    private CorsWebFilter corsWebFilter;
    private WebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        corsWebFilter = new CorsWebFilter();
        filterChain = mock(WebFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void testFilterWithNoOriginHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(exchange);
        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testFilterWithAllowedOrigin() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173,http://localhost:3000");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost:5173",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void testFilterWithDisallowedOrigin() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://evil.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void testPreflightRequest() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:3000");

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("http://localhost:3000",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNotNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertNotNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void testWildcardPortMatching() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        StepVerifier.create(corsWebFilter.filter(exchange1, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost:5173",
                exchange1.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(corsWebFilter.filter(exchange2, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost:3000",
                exchange2.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testWildcardPortMatchingWithoutPort() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testPatternMatchingWithWildcard() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "https://*.example.com");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "https://app.example.com")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals("https://app.example.com",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testExactOriginMatch() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost:5173",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testMultipleAllowedOrigins() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig",
                "http://localhost:5173,http://localhost:3000,https://app.example.com");

        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        StepVerifier.create(corsWebFilter.filter(exchange1, filterChain))
                .expectComplete()
                .verify();

        assertEquals("http://localhost:5173",
                exchange1.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "https://app.example.com")
                .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(corsWebFilter.filter(exchange2, filterChain))
                .expectComplete()
                .verify();

        assertEquals("https://app.example.com",
                exchange2.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testCorsHeadersIncludeVary() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertTrue(exchange.getResponse().getHeaders().get(HttpHeaders.VARY).contains(HttpHeaders.ORIGIN));
    }

    @Test
    void testCorsHeadersIncludeMaxAge() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals("3600",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_MAX_AGE));
    }

    @Test
    void testCorsHeadersIncludeExposedHeaders() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        String exposedHeaders = exchange.getResponse().getHeaders()
                .getFirst(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
        assertNotNull(exposedHeaders);
        assertTrue(exposedHeaders.contains("X-Request-Id"));
        assertTrue(exposedHeaders.contains("X-RateLimit-Remaining"));
    }

    @Test
    void testNonPreflightRequestContinuesChain() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .post("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(exchange);
        assertEquals("http://localhost:5173",
                exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void testInvalidWildcardPortPattern() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:abc")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void testAllowedMethodsIncluded() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        String allowedMethods = exchange.getResponse().getHeaders()
                .getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        assertNotNull(allowedMethods);
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("DELETE"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("PATCH"));
    }

    @Test
    void testAllowedHeadersIncluded() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:5173");

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8090/api/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(corsWebFilter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        String allowedHeaders = exchange.getResponse().getHeaders()
                .getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
        assertNotNull(allowedHeaders);
        assertTrue(allowedHeaders.contains("Authorization"));
        assertTrue(allowedHeaders.contains("Content-Type"));
    }
}