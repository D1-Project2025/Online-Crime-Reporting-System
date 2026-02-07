package com.ocrs.backend.controller;

import com.ocrs.backend.client.AuthServiceClient;
import com.ocrs.backend.dto.AnalyticsResponse;
import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.entity.FIR;
import com.ocrs.backend.entity.MissingPerson;
import com.ocrs.backend.service.AnalyticsService;
import com.ocrs.backend.service.ExternalServiceClient;
import com.ocrs.backend.service.FIRService;
import com.ocrs.backend.service.MissingPersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller for managing FIRs, Missing Person reports, and viewing
 * authorities.
 * 
 * Security: All endpoints require ADMIN role - enforced at both:
 * - API Gateway level (route-based filtering)
 * - Method level (@PreAuthorize annotations for defense-in-depth)
 * 
 * Note: Authority creation/update/delete should be done via Auth Service
 * directly.
 * This controller only reads authority data via Feign for display purposes.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Class-level security - all methods require ADMIN role
public class AdminController {

        private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

        @Autowired
        private FIRService firService;

        @Autowired
        private MissingPersonService missingPersonService;

        @Autowired
        private AnalyticsService analyticsService;

        @Autowired
        private AuthServiceClient authServiceClient;

        @Autowired
        private ExternalServiceClient externalServiceClient;

        // ==================== Analytics ====================

        /**
         * Retrieve system-wide analytics for FIRs and missing person reports.
         *
         * @return the analytics response containing aggregated FIR and missing-person statistics
         */
        @GetMapping("/analytics")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<AnalyticsResponse> getAnalytics() {
                return ResponseEntity.ok(analyticsService.getAnalytics());
        }

        // ==================== FIR Management ====================

        /**
         * Retrieve all FIR records.
         *
         * @return a list containing all FIRs
         */
        @GetMapping("/firs")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<FIR>> getAllFIRs() {
                return ResponseEntity.ok(firService.getAllFIRs());
        }

        /**
         * Retrieve a FIR by its identifier.
         *
         * @param id the FIR identifier
         * @return an ApiResponse containing the requested FIR and response metadata
         */
        @GetMapping("/fir/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FIR>> getFIR(@PathVariable Long id) {
                return ResponseEntity.ok(firService.getFIRById(id));
        }

        /**
         * Reassigns an existing FIR to a different authority.
         *
         * @param firId       the ID of the FIR to reassign
         * @param authorityId the ID of the authority to assign the FIR to
         * @return a ResponseEntity containing an ApiResponse<FIR>; on success the ApiResponse contains the reassigned FIR and the response is HTTP 200, on failure the ApiResponse contains error details and the response is HTTP 400
         */
        @PutMapping("/fir/{firId}/reassign/{authorityId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<FIR>> reassignFIR(
                        @PathVariable Long firId,
                        @PathVariable Long authorityId) {
                ApiResponse<FIR> response = firService.reassignFIR(firId, authorityId);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        // ==================== Missing Person Management ====================

        /**
         * Retrieves all missing person reports.
         *
         * @return a list of all MissingPerson reports in the system
         */
        @GetMapping("/missing-reports")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<MissingPerson>> getAllMissingReports() {
                return ResponseEntity.ok(missingPersonService.getAllReports());
        }

        /**
         * Retrieve a MissingPerson report by its identifier.
         *
         * @param id the identifier of the missing person report to fetch
         * @return the ApiResponse wrapping the requested MissingPerson
         */
        @GetMapping("/missing/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<MissingPerson>> getMissingReport(@PathVariable Long id) {
                return ResponseEntity.ok(missingPersonService.getReportById(id));
        }

        /**
         * Reassigns a missing person report to the authority identified by {@code authorityId}.
         *
         * @param reportId    the ID of the missing person report to reassign
         * @param authorityId the ID of the authority to assign the report to
         * @return an ApiResponse containing the updated MissingPerson when successful, or an ApiResponse with error details otherwise
         */
        @PutMapping("/missing/{reportId}/reassign/{authorityId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<MissingPerson>> reassignMissingReport(
                        @PathVariable Long reportId,
                        @PathVariable Long authorityId) {
                ApiResponse<MissingPerson> response = missingPersonService.reassignReport(reportId, authorityId);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        // ==================== Authority Management (Read-Only) ====================

        /**
         * Retrieve all authorities from the Auth Service.
         *
         * On success returns an ApiResponse wrapping the list of AuthorityDTOs. If the Auth Service call fails,
         * returns an error ApiResponse with the message "Failed to fetch authorities".
         *
         * @return ApiResponse containing the list of AuthorityDTO on success, or an error ApiResponse on failure.
         */
        @GetMapping("/authorities")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<AuthorityDTO>>> getAllAuthorities() {
                try {
                        ApiResponse<List<AuthorityDTO>> response = authServiceClient.getAllAuthorities();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        logger.error("Failed to fetch authorities from Auth service: {}", e.getMessage());
                        return ResponseEntity.ok(ApiResponse.error("Failed to fetch authorities"));
                }
        }

        /**
         * Retrieve active authorities from the Auth Service.
         *
         * Typically used when assigning cases to authorities.
         *
         * @return an ApiResponse containing the list of active AuthorityDTO objects, or an error ApiResponse if retrieval fails
         */
        @GetMapping("/authorities/active")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<List<AuthorityDTO>>> getActiveAuthorities() {
                try {
                        ApiResponse<List<AuthorityDTO>> response = authServiceClient.getActiveAuthorities();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        logger.error("Failed to fetch active authorities from Auth service: {}", e.getMessage());
                        return ResponseEntity.ok(ApiResponse.error("Failed to fetch authorities"));
                }
        }

        /**
         * Fetches an authority by its ID from the Auth Service.
         *
         * @param id the authority's unique identifier
         * @return an ApiResponse containing the AuthorityDTO when the fetch succeeds; an ApiResponse with an error message otherwise
         */
        @GetMapping("/authority/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<AuthorityDTO>> getAuthority(@PathVariable Long id) {
                try {
                        ApiResponse<AuthorityDTO> response = authServiceClient.getAuthorityById(id);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        logger.error("Failed to fetch authority from Auth service: {}", e.getMessage());
                        return ResponseEntity.ok(ApiResponse.error("Failed to fetch authority"));
                }
        }

        // ==================== Deprecated Endpoints ====================

        /**
         * @deprecated Authority creation should be done via Auth Service directly.
         *             This endpoint is kept for backward compatibility but returns an
         *             error.
         */
        @PostMapping("/authority")
        @Deprecated
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> createAuthority(@RequestBody Object request) {
                return ResponseEntity.badRequest()
                                .body(ApiResponse.error(
                                                "Authority creation should be done via Auth Service at /api/auth/authority/register"));
        }

        /**
         * Rejects requests to modify an authority and directs callers to use the Auth Service.
         *
         * @param id      the identifier of the authority requested to be updated
         * @param request the update payload (ignored; updates are not allowed here)
         * @return        a 400 Bad Request ResponseEntity containing an error ApiResponse stating that authority updates must be performed via the Auth Service
         * @deprecated Authority updates must be performed via the Auth Service (e.g., POST /api/auth/authority/register); this endpoint always rejects update attempts.
         */
        @PutMapping("/authority/{id}")
        @Deprecated
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> updateAuthority(@PathVariable Long id, @RequestBody Object request) {
                return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Authority updates should be done via Auth Service"));
        }

        /**
         * Rejects requests to delete an authority and directs callers to the Auth Service for authority management.
         *
         * @param id the identifier of the authority to delete
         * @return an ApiResponse with an error message indicating deletion must be performed via the Auth Service (returned with HTTP 400)
         * @deprecated Authority deletion must be performed through the Auth Service API instead of this controller.
         */
        @DeleteMapping("/authority/{id}")
        @Deprecated
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteAuthority(@PathVariable Long id) {
                return ResponseEntity.badRequest()
                                .body(ApiResponse.error("Authority deletion should be done via Auth Service"));
        }
}