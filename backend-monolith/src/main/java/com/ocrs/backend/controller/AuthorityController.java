package com.ocrs.backend.controller;

import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityAnalyticsResponse;
import com.ocrs.backend.dto.UpdateRequest;
import com.ocrs.backend.entity.FIR;
import com.ocrs.backend.entity.MissingPerson;
import com.ocrs.backend.entity.Update;
import com.ocrs.backend.service.AnalyticsService;
import com.ocrs.backend.service.FIRService;
import com.ocrs.backend.service.MissingPersonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Authority-specific operations.
 * Handles FIR and Missing Person case management for assigned authorities.
 * 
 * Security: All endpoints require AUTHORITY role - enforced at both:
 * - API Gateway level (route-based filtering)
 * - Method level (@PreAuthorize annotations for defense-in-depth)
 */
@RestController
@RequestMapping("/api/authority")
@PreAuthorize("hasRole('AUTHORITY')") // Class-level security - all methods require AUTHORITY role
public class AuthorityController {

        @Autowired
        private FIRService firService;

        @Autowired
        private MissingPersonService missingPersonService;

        @Autowired
        private AnalyticsService analyticsService;

        // ==================== Analytics ====================

        /**
         * Retrieve analytics for the current authority's assigned cases.
         *
         * @param httpRequest the HTTP request containing the current authority's ID in the "userId" attribute
         * @return an AuthorityAnalyticsResponse containing aggregated metrics for the authority's assigned cases
         */
        @GetMapping("/analytics")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<AuthorityAnalyticsResponse> getAnalytics(HttpServletRequest httpRequest) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                return ResponseEntity.ok(analyticsService.getAuthorityAnalytics(authorityId));
        }

        // ==================== FIR Endpoints ====================

        /**
         * Retrieve FIRs assigned to the current authority.
         *
         * @param httpRequest HTTP request containing the current authority's "userId" request attribute
         * @return the list of FIRs assigned to the authority
         */
        @GetMapping("/firs")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<List<FIR>> getAssignedFIRs(HttpServletRequest httpRequest) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                return ResponseEntity.ok(firService.getFIRsByAuthority(authorityId));
        }

        /**
         * Retrieve a paginated page of FIRs assigned to the current authority.
         *
         * @param httpRequest the current HTTP request; the authority's ID is read from the request attribute "userId"
         * @param page        zero-based page index
         * @param size        number of items per page
         * @param sortBy      field name to sort by
         * @param sortDir     sort direction, either "asc" or "desc"
         * @return            a page of FIRs assigned to the authority
         */
        @GetMapping("/firs/paged")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<Page<FIR>> getAssignedFIRsPaged(
                        HttpServletRequest httpRequest,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(firService.getFIRsByAuthorityPaged(authorityId, pageable));
        }

        /**
         * Searches FIRs assigned to the current authority using optional text and enum filters and returns a paginated result.
         *
         * @param httpRequest the HTTP servlet request containing the current authority's "userId" attribute
         * @param search      optional text to match against FIR fields (e.g., complainant, description)
         * @param category    optional FIR category to filter by
         * @param priority    optional FIR priority to filter by
         * @param status      optional FIR status to filter by
         * @param page        zero-based page index
         * @param size        number of items per page
         * @param sortBy      field name to sort by
         * @param sortDir     sort direction, either "asc" or "desc"
         * @return            a page of FIRs assigned to the authority that match the provided filters
         */
        @GetMapping("/firs/search")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<Page<FIR>> searchFIRs(
                        HttpServletRequest httpRequest,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) FIR.Category category,
                        @RequestParam(required = false) FIR.Priority priority,
                        @RequestParam(required = false) FIR.Status status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(firService.searchFIRsByAuthority(authorityId, search, category, priority,
                                status, pageable));
        }

        /**
         * Retrieve a specific FIR by its identifier.
         *
         * @param id the FIR's identifier
         * @return an ApiResponse containing the FIR when found and operation metadata indicating success or failure
         */
        @GetMapping("/fir/{id}")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<ApiResponse<FIR>> getFIR(@PathVariable Long id) {
                return ResponseEntity.ok(firService.getFIRById(id));
        }

        /**
         * Update the status of a FIR and add an update note.
         *
         * Only the authority assigned to the FIR may perform this update.
         *
         * @param firId  the ID of the FIR to update
         * @param request  the update details, including the new status and an optional note
         * @return an ApiResponse containing the updated FIR when the update succeeds, or error details otherwise
         */
        @PutMapping("/fir/{firId}/update")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<ApiResponse<FIR>> updateFIRStatus(
                        @PathVariable Long firId,
                        @Valid @RequestBody UpdateRequest request,
                        HttpServletRequest httpRequest) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                ApiResponse<FIR> response = firService.updateFIRStatus(firId, authorityId, request);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        /**
         * Retrieves status updates for the specified FIR.
         *
         * @param firId the identifier of the FIR
         * @return a list of updates for the specified FIR
         */
        @GetMapping("/fir/{firId}/updates")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<List<Update>> getFIRUpdates(@PathVariable Long firId) {
                return ResponseEntity.ok(firService.getFIRUpdates(firId));
        }

        // ==================== Missing Person Endpoints ====================

        /**
         * Retrieve missing person reports assigned to the current authority.
         *
         * @return a list of MissingPerson reports assigned to the authenticated authority
         */
        @GetMapping("/missing-reports")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<List<MissingPerson>> getAssignedMissingReports(HttpServletRequest httpRequest) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                return ResponseEntity.ok(missingPersonService.getReportsByAuthority(authorityId));
        }

        /**
         * Retrieve a page of Missing Person reports assigned to the current authority.
         *
         * @param httpRequest the HTTP request whose "userId" attribute provides the current authority's ID
         * @param page        zero-based page index
         * @param size        number of items per page
         * @param sortBy      field name to sort by
         * @param sortDir     sort direction, either "asc" or "desc"
         * @return            a page of MissingPerson reports assigned to the current authority
         */
        @GetMapping("/missing-reports/paged")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<Page<MissingPerson>> getAssignedMissingReportsPaged(
                        HttpServletRequest httpRequest,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(missingPersonService.getReportsByAuthorityPaged(authorityId, pageable));
        }

        /**
         * Search and filter missing-person reports assigned to the current authority.
         *
         * @param search  optional text to search within matching report fields (e.g., name, description)
         * @param status  optional missing-person report status to filter by
         * @param page    zero-based page index
         * @param size    number of items per page
         * @param sortBy  field name to sort the results by
         * @param sortDir sort direction, either "asc" or "desc"
         * @return        a page of MissingPerson reports matching the provided filters for the current authority
         */
        @GetMapping("/missing-reports/search")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<Page<MissingPerson>> searchMissingReports(
                        HttpServletRequest httpRequest,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) MissingPerson.MissingStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(
                                missingPersonService.searchReportsByAuthority(authorityId, search, status, pageable));
        }

        /**
         * Retrieve a Missing Person report by its identifier.
         *
         * @param id the missing person report ID to retrieve
         * @return an ApiResponse containing the requested MissingPerson or error details
         */
        @GetMapping("/missing/{id}")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<ApiResponse<MissingPerson>> getMissingReport(@PathVariable Long id) {
                return ResponseEntity.ok(missingPersonService.getReportById(id));
        }

        /**
         * Update a missing person report's status and append an update note.
         *
         * Only the authority assigned to the report may perform this update.
         *
         * @param reportId the identifier of the missing person report to update
         * @param request  the update payload containing the new status and an optional note
         * @return         an ApiResponse containing the updated MissingPerson when successful, or an error description otherwise
         */
        @PutMapping("/missing/{reportId}/update")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<ApiResponse<MissingPerson>> updateMissingReportStatus(
                        @PathVariable Long reportId,
                        @Valid @RequestBody UpdateRequest request,
                        HttpServletRequest httpRequest) {
                Long authorityId = (Long) httpRequest.getAttribute("userId");
                ApiResponse<MissingPerson> response = missingPersonService.updateReportStatus(reportId, authorityId,
                                request);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        /**
         * Retrieves status updates for the specified missing person report.
         *
         * @param reportId the ID of the missing person report
         * @return a list of Update entries for the report
         */
        @GetMapping("/missing/{reportId}/updates")
        @PreAuthorize("hasRole('AUTHORITY')")
        public ResponseEntity<List<Update>> getMissingReportUpdates(@PathVariable Long reportId) {
                return ResponseEntity.ok(missingPersonService.getReportUpdates(reportId));
        }
}