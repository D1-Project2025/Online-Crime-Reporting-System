package com.ocrs.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RateLimitConfig Tests")
class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rateLimitConfig = new RateLimitConfig();
        when(exchange.getRequest()).thenReturn(request);
    }

    @Test
    @DisplayName("Should create ipKeyResolver bean")
    void testIpKeyResolverBeanCreation() {
        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        assertNotNull(resolver);
    }

    @Test
    @DisplayName("Should create userKeyResolver bean")
    void testUserKeyResolverBeanCreation() {
        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        assertNotNull(resolver);
    }

    @Test
    @DisplayName("ipKeyResolver should resolve client IP address")
    void testIpKeyResolverWithValidIp() {
        InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.100", 8080);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("192.168.1.100")
            .verifyComplete();
    }

    @Test
    @DisplayName("ipKeyResolver should return anonymous when remote address is null")
    void testIpKeyResolverWithNullRemoteAddress() {
        when(request.getRemoteAddress()).thenReturn(null);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("anonymous")
            .verifyComplete();
    }

    @Test
    @DisplayName("ipKeyResolver should handle IPv4 addresses")
    void testIpKeyResolverWithIPv4() {
        InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.1", 80);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("10.0.0.1")
            .verifyComplete();
    }

    @Test
    @DisplayName("ipKeyResolver should handle IPv6 addresses")
    void testIpKeyResolverWithIPv6() {
        InetSocketAddress remoteAddress = new InetSocketAddress("::1", 8080);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .assertNext(ip -> assertTrue(ip.contains(":") || ip.equals("0:0:0:0:0:0:0:1")))
            .verifyComplete();
    }

    @Test
    @DisplayName("ipKeyResolver should handle localhost")
    void testIpKeyResolverWithLocalhost() {
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("127.0.0.1")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should resolve user ID from header")
    void testUserKeyResolverWithValidUserId() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("12345");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("12345")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should return anonymous when X-User-Id header is null")
    void testUserKeyResolverWithNullUserId() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn(null);

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("anonymous")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should return anonymous when X-User-Id header is empty")
    void testUserKeyResolverWithEmptyUserId() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("anonymous")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should handle numeric user IDs")
    void testUserKeyResolverWithNumericUserId() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("999");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("999")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should handle string user IDs")
    void testUserKeyResolverWithStringUserId() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("user-abc-123");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("user-abc-123")
            .verifyComplete();
    }

    @Test
    @DisplayName("Should create both key resolvers independently")
    void testBothResolversCreated() {
        KeyResolver ipResolver = rateLimitConfig.ipKeyResolver();
        KeyResolver userResolver = rateLimitConfig.userKeyResolver();

        assertNotNull(ipResolver);
        assertNotNull(userResolver);
        assertNotSame(ipResolver, userResolver);
    }

    @Test
    @DisplayName("ipKeyResolver should handle different ports for same IP")
    void testIpKeyResolverWithDifferentPorts() {
        InetSocketAddress remoteAddress1 = new InetSocketAddress("192.168.1.1", 8080);
        InetSocketAddress remoteAddress2 = new InetSocketAddress("192.168.1.1", 9090);

        when(request.getRemoteAddress()).thenReturn(remoteAddress1);
        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result1 = resolver.resolve(exchange);

        StepVerifier.create(result1)
            .expectNext("192.168.1.1")
            .verifyComplete();

        when(request.getRemoteAddress()).thenReturn(remoteAddress2);
        Mono<String> result2 = resolver.resolve(exchange);

        StepVerifier.create(result2)
            .expectNext("192.168.1.1")
            .verifyComplete();
    }

    @Test
    @DisplayName("userKeyResolver should handle special characters in user ID")
    void testUserKeyResolverWithSpecialCharacters() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("user@example.com");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        StepVerifier.create(result)
            .expectNext("user@example.com")
            .verifyComplete();
    }

    @Test
    @DisplayName("ipKeyResolver should return reactive Mono")
    void testIpKeyResolverReturnsReactiveMono() {
        InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.1", 8080);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        assertNotNull(result);
        assertTrue(result instanceof Mono);
    }

    @Test
    @DisplayName("userKeyResolver should return reactive Mono")
    void testUserKeyResolverReturnsReactiveMono() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("123");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();
        Mono<String> result = resolver.resolve(exchange);

        assertNotNull(result);
        assertTrue(result instanceof Mono);
    }

    @Test
    @DisplayName("ipKeyResolver should handle rapid successive calls")
    void testIpKeyResolverMultipleCalls() {
        InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.50", 8080);
        when(request.getRemoteAddress()).thenReturn(remoteAddress);

        KeyResolver resolver = rateLimitConfig.ipKeyResolver();

        for (int i = 0; i < 10; i++) {
            Mono<String> result = resolver.resolve(exchange);
            StepVerifier.create(result)
                .expectNext("192.168.1.50")
                .verifyComplete();
        }
    }

    @Test
    @DisplayName("userKeyResolver should handle rapid successive calls")
    void testUserKeyResolverMultipleCalls() {
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-User-Id")).thenReturn("testuser");

        KeyResolver resolver = rateLimitConfig.userKeyResolver();

        for (int i = 0; i < 10; i++) {
            Mono<String> result = resolver.resolve(exchange);
            StepVerifier.create(result)
                .expectNext("testuser")
                .verifyComplete();
        }
    }
}