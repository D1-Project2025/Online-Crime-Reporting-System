package com.ocrs.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    private final RateLimitConfig rateLimitConfig = new RateLimitConfig();

    @Test
    void testIpKeyResolverWithValidIp() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();
        assertNotNull(ipKeyResolver);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("192.168.1.100")
                .verifyComplete();
    }

    @Test
    void testIpKeyResolverWithNullRemoteAddress() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("anonymous")
                .verifyComplete();
    }

    @Test
    void testIpKeyResolverWithLocalhost() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 54321))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("127.0.0.1")
                .verifyComplete();
    }

    @Test
    void testIpKeyResolverWithIpv6() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .assertNext(ip -> {
                    assertNotNull(ip);
                    assertTrue(ip.contains(":") || ip.equals("anonymous"));
                })
                .verifyComplete();
    }

    @Test
    void testUserKeyResolverWithUserIdHeader() {
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();
        assertNotNull(userKeyResolver);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header("X-User-Id", "12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("12345")
                .verifyComplete();
    }

    @Test
    void testUserKeyResolverWithoutUserIdHeader() {
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("anonymous")
                .verifyComplete();
    }

    @Test
    void testUserKeyResolverWithEmptyUserIdHeader() {
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header("X-User-Id", "")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("anonymous")
                .verifyComplete();
    }

    @Test
    void testBothResolversReturnDifferentKeys() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 9999))
                .header("X-User-Id", "user-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("10.0.0.1")
                .verifyComplete();

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("user-123")
                .verifyComplete();
    }

    @Test
    void testIpKeyResolverConsistency() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("192.168.1.50", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("192.168.1.50")
                .verifyComplete();

        StepVerifier.create(ipKeyResolver.resolve(exchange))
                .expectNext("192.168.1.50")
                .verifyComplete();
    }

    @Test
    void testUserKeyResolverConsistency() {
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header("X-User-Id", "user-999")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("user-999")
                .verifyComplete();

        StepVerifier.create(userKeyResolver.resolve(exchange))
                .expectNext("user-999")
                .verifyComplete();
    }

    @Test
    void testMultipleUserIdsUseDifferentKeys() {
        KeyResolver userKeyResolver = rateLimitConfig.userKeyResolver();

        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header("X-User-Id", "user-1")
                .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .header("X-User-Id", "user-2")
                .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(userKeyResolver.resolve(exchange1))
                .expectNext("user-1")
                .verifyComplete();

        StepVerifier.create(userKeyResolver.resolve(exchange2))
                .expectNext("user-2")
                .verifyComplete();
    }

    @Test
    void testMultipleIpsUseDifferentKeys() {
        KeyResolver ipKeyResolver = rateLimitConfig.ipKeyResolver();

        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("http://localhost:8090/api/test")
                .remoteAddress(new InetSocketAddress("10.0.0.2", 8080))
                .build();
        MockServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        StepVerifier.create(ipKeyResolver.resolve(exchange1))
                .expectNext("10.0.0.1")
                .verifyComplete();

        StepVerifier.create(ipKeyResolver.resolve(exchange2))
                .expectNext("10.0.0.2")
                .verifyComplete();
    }
}