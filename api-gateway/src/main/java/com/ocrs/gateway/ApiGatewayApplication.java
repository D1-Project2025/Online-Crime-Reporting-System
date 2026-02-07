package com.ocrs.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

        /**
         * Application entry point that starts the API Gateway Spring Boot application.
         *
         * Bootstraps the Spring application context and begins application execution.
         *
         * @param args command-line arguments passed to the application
         */
        public static void main(String[] args) {
                SpringApplication.run(ApiGatewayApplication.class, args);
        }
}