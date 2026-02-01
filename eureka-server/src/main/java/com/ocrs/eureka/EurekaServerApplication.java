package com.ocrs.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

        /**
         * Application entry point that starts the Eureka Server Spring Boot application.
         *
         * @param args command-line arguments forwarded to the Spring application
         */
        public static void main(String[] args) {
                SpringApplication.run(EurekaServerApplication.class, args);
        }
}