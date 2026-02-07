package com.ocrs.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom AccessDeniedHandler that returns proper JSON error responses
 * for 403 Forbidden errors.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
        private final ObjectMapper objectMapper;

        /**
         * Sends a standardized JSON 403 Forbidden response when an access-denied event occurs.
         *
         * The response body contains `success` (false), a fixed `message` ("Access Denied: Insufficient permissions"),
         * the request `path`, and a `timestamp`. The handler also logs the denied request and exception message.
         *
         * @param request the HTTP request that triggered the access denial
         * @param response the HTTP response used to write the JSON error payload
         * @param accessDeniedException the exception that caused the access denial
         * @throws IOException if an I/O error occurs while writing the response
         * @throws ServletException if a servlet error occurs while handling the request
         */
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
                logger.warn("Access denied to {}: {}", request.getRequestURI(),
                                accessDeniedException.getMessage());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("success", false);
                body.put("message", "Access Denied: Insufficient permissions");
                body.put("path", request.getRequestURI());
                body.put("timestamp", LocalDateTime.now().toString());

                objectMapper.writeValue(response.getOutputStream(), body);
        }
}