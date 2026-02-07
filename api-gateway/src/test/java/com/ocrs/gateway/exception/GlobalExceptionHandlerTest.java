package com.ocrs.gateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private org.springframework.http.HttpHeaders responseHeaders;

    private DataBufferFactory bufferFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        bufferFactory = new DefaultDataBufferFactory();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(responseHeaders);
        when(response.bufferFactory()).thenReturn(bufferFactory);
        when(request.getPath()).thenReturn(mock(org.springframework.http.server.RequestPath.class));
        when(request.getPath().toString()).thenReturn("/api/test");
        when(response.writeWith(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with UNAUTHORIZED")
    void testHandleUnauthorizedException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
        verify(response).writeWith(any());
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with FORBIDDEN")
    void testHandleForbiddenException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with NOT_FOUND")
    void testHandleNotFoundException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle NotFoundException from Gateway")
    void testHandleGatewayNotFoundException() {
        NotFoundException ex = new NotFoundException("Service not found");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle ConnectException")
    void testHandleConnectException() {
        ConnectException ex = new ConnectException("Connection refused");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle TimeoutException")
    void testHandleTimeoutException() {
        TimeoutException ex = new TimeoutException("Request timed out");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle nested ConnectException")
    void testHandleNestedConnectException() {
        Exception nested = new RuntimeException("Outer exception", new ConnectException("Inner connection failed"));

        StepVerifier.create(handler.handle(exchange, nested))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle generic exception as INTERNAL_SERVER_ERROR")
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with BAD_REQUEST")
    void testHandleBadRequestException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with TOO_MANY_REQUESTS")
    void testHandleTooManyRequestsException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Should include path in error response")
    void testErrorResponseIncludesPath() {
        when(request.getPath().toString()).thenReturn("/api/specific/path");
        Exception ex = new RuntimeException("Test error");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(request.getPath()).toString();
        verify(response).writeWith(any());
    }

    @Test
    @DisplayName("Should set Content-Type to application/json")
    void testContentTypeIsJson() {
        Exception ex = new RuntimeException("Test");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(responseHeaders).setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle ResponseStatusException without reason")
    void testHandleResponseStatusExceptionWithoutReason() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should handle exception with special characters in message")
    void testHandleExceptionWithSpecialCharacters() {
        Exception ex = new RuntimeException("Error with \"quotes\" and \n newlines and \t tabs");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).writeWith(any());
    }

    @Test
    @DisplayName("Should handle null exception message")
    void testHandleExceptionWithNullMessage() {
        Exception ex = new RuntimeException();

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should handle multiple consecutive exceptions")
    void testHandleMultipleExceptions() {
        Exception ex1 = new RuntimeException("First error");
        Exception ex2 = new RuntimeException("Second error");

        StepVerifier.create(handler.handle(exchange, ex1))
            .verifyComplete();

        StepVerifier.create(handler.handle(exchange, ex2))
            .verifyComplete();

        verify(response, times(2)).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should handle SERVICE_UNAVAILABLE status")
    void testHandleServiceUnavailableException() {
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, "Service down");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("Should handle GATEWAY_TIMEOUT status")
    void testHandleGatewayTimeoutException() {
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.GATEWAY_TIMEOUT, "Gateway timeout");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    @DisplayName("Should handle exception with backslash in message")
    void testHandleExceptionWithBackslash() {
        Exception ex = new RuntimeException("Path\\to\\file");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).writeWith(any());
    }

    @Test
    @DisplayName("Should write response buffer")
    void testWriteResponseBuffer() {
        Exception ex = new RuntimeException("Test");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).writeWith(any(Mono.class));
    }

    @Test
    @DisplayName("Should handle very long error messages")
    void testHandleLongErrorMessage() {
        String longMessage = "Error: " + "A".repeat(10000);
        Exception ex = new RuntimeException(longMessage);

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).writeWith(any());
    }

    @Test
    @DisplayName("Should handle different paths correctly")
    void testDifferentPaths() {
        String[] paths = {"/api/auth", "/api/user/123", "/api/admin/dashboard", "/health"};

        for (String path : paths) {
            when(request.getPath().toString()).thenReturn(path);
            Exception ex = new RuntimeException("Error on " + path);

            StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();
        }

        verify(response, times(paths.length)).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return completed Mono")
    void testReturnsCompletedMono() {
        Exception ex = new RuntimeException("Test");

        Mono<Void> result = handler.handle(exchange, ex);

        assertNotNull(result);
        StepVerifier.create(result)
            .verifyComplete();
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Object should not be null");
        }
    }

    @Test
    @DisplayName("Should handle ResponseStatusException with custom reason")
    void testHandleResponseStatusExceptionWithCustomReason() {
        ResponseStatusException ex = new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Custom access denied message");

        StepVerifier.create(handler.handle(exchange, ex))
            .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should handle concurrent exception handling")
    void testConcurrentExceptionHandling() {
        Exception ex = new RuntimeException("Concurrent test");

        Mono<Void> result1 = handler.handle(exchange, ex);
        Mono<Void> result2 = handler.handle(exchange, ex);

        StepVerifier.create(Mono.when(result1, result2))
            .verifyComplete();
    }
}