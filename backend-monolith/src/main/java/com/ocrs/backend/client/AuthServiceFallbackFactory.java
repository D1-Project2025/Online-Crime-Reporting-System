package com.ocrs.backend.client;

import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback factory for AuthServiceClient.
 * Provides fallback responses when Auth service is unavailable.
 */
@Component
public class AuthServiceFallbackFactory implements FallbackFactory<AuthServiceClient> {

        private static final Logger logger = LoggerFactory.getLogger(AuthServiceFallbackFactory.class);

        /**
         * Create a fallback AuthServiceClient used when the Auth service is unavailable.
         *
         * <p>The returned client logs fallback usage and provides safe fallback responses:
         * most methods return an error ApiResponse with an unavailability message, while
         * getAuthorityById returns a success ApiResponse containing a minimal AuthorityDTO
         * populated with the provided id and limited fields.</p>
         *
         * @param cause the throwable that caused the fallback (used for logging)
         * @return an AuthServiceClient that supplies fallback ApiResponse values for each API method
         */
        @Override
        public AuthServiceClient create(Throwable cause) {
                logger.error("Auth service is unavailable. Cause: {}", cause.getMessage());

                return new AuthServiceClient() {

                        @Override
                        public ApiResponse<UserDTO> getUserById(Long id) {
                                logger.warn("Fallback: getUserById({})", id);
                                return ApiResponse.error("Auth service unavailable - cannot fetch user details");
                        }

                        @Override
                        public ApiResponse<UserDTO> getUserByEmail(String email) {
                                logger.warn("Fallback: getUserByEmail({})", email);
                                return ApiResponse.error("Auth service unavailable - cannot fetch user details");
                        }

                        @Override
                        public ApiResponse<AuthorityDTO> getAuthorityById(Long id) {
                                logger.warn("Fallback: getAuthorityById({})", id);
                                // Return a minimal fallback authority with just the ID
                                AuthorityDTO fallbackAuthority = AuthorityDTO.builder()
                                                .id(id)
                                                .fullName("Authority #" + id)
                                                .email("unavailable")
                                                .isActive(true)
                                                .build();
                                return ApiResponse.success("Fallback: limited authority data", fallbackAuthority);
                        }

                        @Override
                        public ApiResponse<List<AuthorityDTO>> getAllAuthorities() {
                                logger.warn("Fallback: getAllAuthorities()");
                                return ApiResponse.error("Auth service unavailable - cannot fetch authorities list");
                        }

                        @Override
                        public ApiResponse<List<AuthorityDTO>> getActiveAuthorities() {
                                logger.warn("Fallback: getActiveAuthorities()");
                                return ApiResponse.error("Auth service unavailable - cannot fetch authorities list");
                        }
                };
        }
}