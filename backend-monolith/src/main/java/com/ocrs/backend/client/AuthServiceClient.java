package com.ocrs.backend.client;

import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with Auth Service.
 * Uses Eureka service discovery to find auth-service instances.
 * Falls back to AuthServiceFallback when auth-service is unavailable.
 */

@FeignClient(name = "auth-service", fallbackFactory = AuthServiceFallbackFactory.class)
public interface AuthServiceClient {

        /**
         * Fetches a user's details by their identifier.
         *
         * @param id the user's unique identifier
         * @return the ApiResponse wrapping the UserDTO for the given id, or an error payload if the user is not found
         */
        @GetMapping("/api/internal/users/{id}")
        ApiResponse<UserDTO> getUserById(@PathVariable("id") Long id);

        /**
         * Retrieve user details for the specified email address.
         *
         * @param email the user's email address
         * @return an ApiResponse containing the UserDTO for the specified user
         */
        @GetMapping("/api/internal/users/email/{email}")
        ApiResponse<UserDTO> getUserByEmail(@PathVariable("email") String email);

        /**
         * Retrieve authority details for a given authority identifier.
         *
         * @param id the identifier of the authority to retrieve
         * @return an ApiResponse containing the AuthorityDTO for the specified id
         */
        @GetMapping("/api/internal/authorities/{id}")
        ApiResponse<AuthorityDTO> getAuthorityById(@PathVariable("id") Long id);

        /**
         * Retrieves all authority records from the auth service.
         *
         * @return an ApiResponse containing a list of AuthorityDTO objects representing every authority.
         */
        @GetMapping("/api/internal/authorities")
        ApiResponse<List<AuthorityDTO>> getAllAuthorities();

        /**
         * Retrieve all active authority records.
         *
         * @return an {@link ApiResponse} containing the list of active {@link AuthorityDTO} objects
         */
        @GetMapping("/api/internal/authorities/active")
        ApiResponse<List<AuthorityDTO>> getActiveAuthorities();
}