package com.ocrs.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private GatewayFilterChain filterChain;
    private static final String TEST_SECRET = "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long-for-hmac-sha";

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter();
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtSecret", TEST_SECRET);
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    private String generateToken(Long userId, String email, String role, boolean expired) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        Instant expiration = expired ? now.minus(1, ChronoUnit.HOURS) : now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(email)
                .claim("id", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    @Test
    void testOptionsRequestSkipsJwtValidation() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, "/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void testMissingAuthorizationHeader() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testInvalidAuthorizationHeaderFormat() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testValidToken() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(123L, "user@example.com", "USER", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void testExpiredToken() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(123L, "user@example.com", "USER", true);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testMalformedToken() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer malformed.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testRoleCheckWithMatchingRole() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(456L, "admin@example.com", "ADMIN", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void testRoleCheckWithMismatchedRole() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(123L, "user@example.com", "USER", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void testRoleCheckCaseInsensitive() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("admin");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(789L, "admin@example.com", "ADMIN", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void testUserHeadersAddedToRequest() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(999L, "test@example.com", "USER", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain).filter(argThat(modifiedExchange -> {
            String userId = modifiedExchange.getRequest().getHeaders().getFirst("X-User-Id");
            String userEmail = modifiedExchange.getRequest().getHeaders().getFirst("X-User-Email");
            String userRole = modifiedExchange.getRequest().getHeaders().getFirst("X-User-Role");

            return "999".equals(userId) &&
                   "test@example.com".equals(userEmail) &&
                   "USER".equals(userRole);
        }));
    }

    @Test
    void testNoRoleRequirement() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(111L, "any@example.com", "USER", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/public")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void testEmptyRoleRequirement() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(222L, "user@example.com", "USER", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/resource")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void testTokenWithMissingRole() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenWithoutRole = Jwts.builder()
                .subject("user@example.com")
                .claim("id", 333L)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithoutRole)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void testMultipleRequestsWithDifferentTokens() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token1 = generateToken(100L, "user1@example.com", "USER", false);
        String token2 = generateToken(200L, "user2@example.com", "ADMIN", false);

        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("/api/test1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token1)
                .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("/api/test2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token2)
                .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(filter.filter(exchange1, filterChain))
                .expectComplete()
                .verify();

        StepVerifier.create(filter.filter(exchange2, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(2)).filter(any());
    }

    @Test
    void testAuthorityRoleAccess() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("AUTHORITY");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        String token = generateToken(500L, "authority@police.gov", "AUTHORITY", false);
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/authority/fir/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .expectComplete()
                .verify();

        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void testConfigGettersAndSetters() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();

        assertNull(config.getRequiredRole());

        config.setRequiredRole("TEST_ROLE");
        assertEquals("TEST_ROLE", config.getRequiredRole());

        config.setRequiredRole("ANOTHER_ROLE");
        assertEquals("ANOTHER_ROLE", config.getRequiredRole());
    }
}