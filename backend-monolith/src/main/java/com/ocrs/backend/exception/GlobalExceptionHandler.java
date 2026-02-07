package com.ocrs.backend.exception;

import com.ocrs.backend.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// global exception handler for clean, consistent error responses
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Handle validation failures from @Valid-annotated controller inputs.
         *
         * Extracts field-level validation messages and responds with HTTP 400 containing
         * an ApiResponse whose error message is the first validation error.
         *
         * @param ex the MethodArgumentNotValidException containing binding errors
         * @return a ResponseEntity with status 400 and an ApiResponse describing the first validation error
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                logger.warn("validation failed: {}", errors);
                return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Validation failed: " + errors.values().iterator().next()));
        }

        /**
         * Handles ResourceNotFoundException and produces an HTTP 404 response with the exception message.
         *
         * @param ex the ResourceNotFoundException that triggered this handler
         * @return a ResponseEntity with status 404 and an ApiResponse containing the exception's message
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
                logger.warn("resource not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Handle IllegalArgumentException by responding with HTTP 400 Bad Request and the exception message.
         *
         * @param ex the IllegalArgumentException that triggered this handler
         * @return a ResponseEntity containing an ApiResponse with the exception message and HTTP 400 status
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
                logger.warn("invalid argument: {}", ex.getMessage());
                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(ex.getMessage()));
        }

        /**
         * Handle any uncaught exception and produce a standardized 500 Internal Server Error response.
         *
         * @param ex the uncaught exception that triggered this handler
         * @return ResponseEntity containing an ApiResponse with a generic error message and HTTP 500 (Internal Server Error) status
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
                logger.error("unexpected error: {}", ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
        }
}