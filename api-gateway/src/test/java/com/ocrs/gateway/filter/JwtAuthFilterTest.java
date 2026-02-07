package com.ocrs.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthFilter Tests")
class JwtAuthFilterTest {

    private JwtAuthFilter jwtAuthFilter;
    private String jwtSecret;
    private SecretKey key;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private HttpHeaders requestHeaders;

    @Mock
    private org.springframework.http.HttpHeaders responseHeaders;

    @Mock
    private ServerHttpRequest.Builder requestBuilder;

    @Mock
    private ServerWebExchange.Builder exchangeBuilder;

    private DataBufferFactory bufferFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthFilter = new JwtAuthFilter();
        jwtSecret = "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long";
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtSecret", jwtSecret);

        bufferFactory = new DefaultDataBufferFactory();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getHeaders()).thenReturn(requestHeaders);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(response.bufferFactory()).thenReturn(bufferFactory);
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/test");
        when(response.writeWith(any())).thenReturn(Mono.empty());
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should skip JWT validation for OPTIONS preflight request")
    void testSkipValidationForOptions() {
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when Authorization header is missing")
    void testMissingAuthorizationHeader() {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when Authorization header format is invalid")
    void testInvalidAuthorizationHeaderFormat() {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidFormat token");
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when token is expired")
    void testExpiredToken() {
        String expiredToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
            .expiration(Date.from(Instant.now().minusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + expiredToken);
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when token is malformed")
    void testMalformedToken() {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer malformed.token.here");
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should pass through with valid token")
    void testValidToken() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("Should return FORBIDDEN when role is insufficient")
    void testInsufficientRole() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(any());
    }

    @Test
    @DisplayName("Should pass through when role matches required role")
    void testMatchingRole() {
        String validToken = Jwts.builder()
            .subject("admin@example.com")
            .claim("id", 2L)
            .claim("role", "ADMIN")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Should add user info headers to request")
    void testAddUserInfoHeaders() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 123L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(requestBuilder).header("X-User-Id", "123");
        verify(requestBuilder).header("X-User-Email", "user@example.com");
        verify(requestBuilder).header("X-User-Role", "USER");
    }

    @Test
    @DisplayName("Should handle token without role claim")
    void testTokenWithoutRole() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Should handle empty Authorization header")
    void testEmptyAuthorizationHeader() {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("");
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should handle null Authorization header value")
    void testNullAuthorizationHeaderValue() {
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should handle Bearer token with extra spaces")
    void testBearerTokenWithExtraSpaces() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer  " + validToken);
        when(response.setComplete()).thenReturn(Mono.empty());

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        // This should fail because of extra space after Bearer
        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Config should support getters and setters")
    void testConfigGettersAndSetters() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();

        assertNull(config.getRequiredRole());

        config.setRequiredRole("ADMIN");
        assertEquals("ADMIN", config.getRequiredRole());

        config.setRequiredRole("USER");
        assertEquals("USER", config.getRequiredRole());

        config.setRequiredRole(null);
        assertNull(config.getRequiredRole());
    }

    @Test
    @DisplayName("Should pass through with no required role configured")
    void testNoRequiredRole() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole(null);
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Should handle POST request with valid token")
    void testPostRequestWithValidToken() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "USER")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    @DisplayName("Should handle role check case insensitivity")
    void testRoleCheckCaseInsensitive() {
        String validToken = Jwts.builder()
            .subject("user@example.com")
            .claim("id", 1L)
            .claim("role", "admin")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(key)
            .compact();

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(requestHeaders.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(true);
        when(requestHeaders.getFirst(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any())).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);

        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        config.setRequiredRole("ADMIN");
        GatewayFilter filter = jwtAuthFilter.apply(config);

        StepVerifier.create(filter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(any());
    }

    private void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("Expected null but got: " + obj);
        }
    }

    private void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + " but got: " + actual);
        }
    }
}