package com.ocrs.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@DisplayName("CorsWebFilter Tests")
class CorsWebFilterTest {

    private CorsWebFilter corsWebFilter;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebFilterChain chain;

    @Mock
    private HttpHeaders requestHeaders;

    @Mock
    private HttpHeaders responseHeaders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        corsWebFilter = new CorsWebFilter();
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig",
            "http://localhost:5173,http://localhost:3000");

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getHeaders()).thenReturn(requestHeaders);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should pass through when no Origin header present")
    void testNoOriginHeader() {
        when(requestHeaders.getOrigin()).thenReturn(null);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    @DisplayName("Should block disallowed origin")
    void testDisallowedOrigin() {
        when(requestHeaders.getOrigin()).thenReturn("http://malicious-site.com");
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(response).setComplete();
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should allow exact match origin")
    void testAllowedOriginExactMatch() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle OPTIONS preflight request")
    void testOptionsPreflightRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:3000");
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
        verify(response).setStatusCode(HttpStatus.OK);
        verify(response).setComplete();
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should add all required CORS headers")
    void testAllCorsHeadersAdded() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), eq("http://localhost:5173"));
        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), eq("true"));
        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), anyString());
        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS), anyString());
        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS), anyString());
        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_MAX_AGE), eq("3600"));
        verify(responseHeaders, atLeastOnce()).add(eq(HttpHeaders.VARY), anyString());
    }

    @Test
    @DisplayName("Should handle wildcard port pattern")
    void testWildcardPortPattern() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        when(requestHeaders.getOrigin()).thenReturn("http://localhost:8080");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8080");
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle localhost without port with wildcard pattern")
    void testLocalhostWithoutPortWildcardPattern() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        when(requestHeaders.getOrigin()).thenReturn("http://localhost");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost");
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle general wildcard pattern")
    void testGeneralWildcardPattern() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://*.example.com");

        when(requestHeaders.getOrigin()).thenReturn("http://api.example.com");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://api.example.com");
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should reject origin not matching wildcard pattern")
    void testWildcardPatternMismatch() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        when(requestHeaders.getOrigin()).thenReturn("http://example.com:8080");
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(chain, never()).filter(exchange);
    }

    @Test
    @DisplayName("Should handle multiple allowed origins")
    void testMultipleAllowedOrigins() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:3000");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000");
        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should add Vary headers for caching")
    void testVaryHeaders() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).add(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        verify(responseHeaders).add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        verify(responseHeaders).add(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
    }

    @Test
    @DisplayName("Should set max age for preflight caching")
    void testMaxAge() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }

    @Test
    @DisplayName("Should handle GET request")
    void testGetRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
        verify(response, never()).setComplete();
    }

    @Test
    @DisplayName("Should handle POST request")
    void testPostRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
        verify(response, never()).setComplete();
    }

    @Test
    @DisplayName("Should handle PUT request")
    void testPutRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.PUT);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle DELETE request")
    void testDeleteRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.DELETE);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should handle PATCH request")
    void testPatchRequest() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.PATCH);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should trim whitespace from allowed origins config")
    void testAllowedOriginsWithWhitespace() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig",
            " http://localhost:5173 , http://localhost:3000 ");

        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    @DisplayName("Should reject wildcard port with non-numeric port")
    void testWildcardPortWithNonNumericValue() {
        ReflectionTestUtils.setField(corsWebFilter, "allowedOriginsConfig", "http://localhost:*");

        when(requestHeaders.getOrigin()).thenReturn("http://localhost:abc");
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should handle origin with different scheme")
    void testDifferentScheme() {
        when(requestHeaders.getOrigin()).thenReturn("https://localhost:5173");
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should expose rate limit headers")
    void testExposedHeaders() {
        when(requestHeaders.getOrigin()).thenReturn("http://localhost:5173");
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        StepVerifier.create(corsWebFilter.filter(exchange, chain))
            .verifyComplete();

        verify(responseHeaders).set(eq(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS), contains("X-RateLimit"));
    }
}