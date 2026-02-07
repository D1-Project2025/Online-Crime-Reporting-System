package com.ocrs.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

        /**
         * Reports that the backend service is running.
         *
         * @return the HTTP 200 response with body "Backend Monolith is running"
         */
        @GetMapping("/health")
        public ResponseEntity<String> health() {
                return ResponseEntity.ok("Backend Monolith is running");
        }
}