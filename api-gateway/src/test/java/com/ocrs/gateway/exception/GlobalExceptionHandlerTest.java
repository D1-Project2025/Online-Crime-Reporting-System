package com.ocrs.gateway.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNotFoundException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/nonexistent")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        NotFoundException exception = new NotFoundException("Service not found");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        assertNotNull(exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void testHandleResponseStatusException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid request");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleConnectException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/service")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ConnectException exception = new ConnectException("Connection refused");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleTimeoutException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/slow")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        TimeoutException exception = new TimeoutException("Request timed out");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleNestedConnectException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/nested")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception exception = new RuntimeException("Wrapper",
                new ConnectException("Connection failed"));

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleGenericException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/error")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception exception = new RuntimeException("Unexpected error");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleUnauthorizedException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/secured")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Not authenticated");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleForbiddenException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Access denied");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleNotFoundHttpException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/missing")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource not found");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void testHandleTooManyRequestsException() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/limited")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
    }

    @Test
    void testResponseStatusExceptionWithNullReason() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void testExceptionWithSpecialCharactersInMessage() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception exception = new RuntimeException("Error with \"quotes\" and \n newlines");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }

    @Test
    void testMultipleDifferentExceptions() {
        Exception[] exceptions = {
                new NotFoundException("Not found"),
                new ConnectException("Connection failed"),
                new TimeoutException("Timeout"),
                new RuntimeException("Generic error")
        };

        HttpStatus[] expectedStatuses = {
                HttpStatus.SERVICE_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE,
                HttpStatus.GATEWAY_TIMEOUT,
                HttpStatus.INTERNAL_SERVER_ERROR
        };

        for (int i = 0; i < exceptions.length; i++) {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/test-" + i)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(exceptionHandler.handle(exchange, exceptions[i]))
                    .expectComplete()
                    .verify();

            assertEquals(expectedStatuses[i], exchange.getResponse().getStatusCode(),
                    "Failed for exception: " + exceptions[i].getClass().getSimpleName());
        }
    }

    @Test
    void testExceptionHandlerPreservesPath() {
        String testPath = "/api/specific/endpoint/path";
        MockServerHttpRequest request = MockServerHttpRequest
                .get(testPath)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Exception exception = new RuntimeException("Test error");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(testPath, request.getPath().toString());
    }

    @Test
    void testServiceUnavailableScenario() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/downstream-service")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "Service is down");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
    }

    @Test
    void testInternalServerErrorScenario() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/crash")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        NullPointerException exception = new NullPointerException("Unexpected null");

        StepVerifier.create(exceptionHandler.handle(exchange, exception))
                .expectComplete()
                .verify();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
    }
}