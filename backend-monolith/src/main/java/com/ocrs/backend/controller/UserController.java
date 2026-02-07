package com.ocrs.backend.controller;

import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.FIRRequest;
import com.ocrs.backend.dto.MissingPersonRequest;
import com.ocrs.backend.entity.FIR;
import com.ocrs.backend.entity.MissingPerson;
import com.ocrs.backend.entity.Update;
import com.ocrs.backend.service.FIRService;
import com.ocrs.backend.service.MissingPersonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for User-specific operations.
 * Handles FIR filing and Missing Person report management for authenticated
 * users.
 * 
 * All endpoints require USER role - enforced at both:
 * - API Gateway level (route-based filtering)
 * - Method level (@PreAuthorize annotations for defense-in-depth)
 */
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')") // Class-level security - all methods require USER role
public class UserController {

        @Autowired
        private FIRService firService;

        @Autowired
        private MissingPersonService missingPersonService;

        // ==================== FIR Endpoints ====================

        /**
         * Create a new FIR for the authenticated user.
         *
         * @param request     FIR details to file
         * @param httpRequest HTTP request whose "userId" attribute identifies the requesting user
         * @return an ApiResponse containing the created FIR on success or error details on failure
         */
        @PostMapping("/fir")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<FIR>> fileFIR(
                        @Valid @RequestBody FIRRequest request,
                        HttpServletRequest httpRequest) {
                Long userId = (Long) httpRequest.getAttribute("userId");
                ApiResponse<FIR> response = firService.fileFIR(userId, request);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        /**
         * Retrieve all FIRs filed by the authenticated user.
         *
         * @param httpRequest the HTTP request containing the authenticated user's id in the "userId" attribute
         * @return a list of FIRs belonging to the authenticated user
         */
        @GetMapping("/firs")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<FIR>> getMyFIRs(HttpServletRequest httpRequest) {
                Long userId = (Long) httpRequest.getAttribute("userId");
                return ResponseEntity.ok(firService.getFIRsByUser(userId));
        }

        /**
         * Retrieve an FIR by its identifier.
         *
         * @param id the FIR's unique identifier
         * @return an ApiResponse containing the FIR when found; the response indicates success or failure
         */
        @GetMapping("/fir/{id}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<FIR>> getFIR(@PathVariable Long id) {
                return ResponseEntity.ok(firService.getFIRById(id));
        }

        /**
         * Retrieves the FIR identified by its FIR number.
         *
         * @param firNumber the FIR's unique case number
         * @return an ApiResponse containing the FIR matching the provided FIR number
         */
        @GetMapping("/fir/number/{firNumber}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<FIR>> getFIRByNumber(@PathVariable String firNumber) {
                return ResponseEntity.ok(firService.getFIRByNumber(firNumber));
        }

        /**
         * Retrieve status updates for the specified FIR.
         *
         * @param firId the identifier of the FIR
         * @return the list of status updates for the specified FIR
         */
        @GetMapping("/fir/{firId}/updates")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<Update>> getFIRUpdates(@PathVariable Long firId) {
                return ResponseEntity.ok(firService.getFIRUpdates(firId));
        }

        // ==================== Missing Person Endpoints ====================

        /**
         * Create a missing person report for the authenticated user.
         *
         * @param request     the payload containing missing person report details
         * @param httpRequest the incoming HTTP request; expects the authenticated user's id in the "userId" request attribute
         * @return            a ResponseEntity whose body is an ApiResponse containing the created MissingPerson on success (HTTP 200) or error details on failure (HTTP 400)
         */
        @PostMapping("/missing")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<MissingPerson>> fileMissingReport(
                        @Valid @RequestBody MissingPersonRequest request,
                        HttpServletRequest httpRequest) {
                Long userId = (Long) httpRequest.getAttribute("userId");
                ApiResponse<MissingPerson> response = missingPersonService.fileReport(userId, request);
                return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
        }

        /**
         * Retrieves all missing-person reports filed by the current authenticated user.
         *
         * @param httpRequest the HTTP request containing the "userId" request attribute used to identify the user
         * @return a list of MissingPerson reports filed by the user
         */
        @GetMapping("/missing-reports")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<MissingPerson>> getMyMissingReports(HttpServletRequest httpRequest) {
                Long userId = (Long) httpRequest.getAttribute("userId");
                return ResponseEntity.ok(missingPersonService.getReportsByUser(userId));
        }

        /**
         * Retrieve a Missing Person report by its ID.
         *
         * @param id the ID of the missing person report
         * @return an ApiResponse containing the requested MissingPerson; `success` will be `true` when the report is found
         */
        @GetMapping("/missing/{id}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<MissingPerson>> getMissingReport(@PathVariable Long id) {
                return ResponseEntity.ok(missingPersonService.getReportById(id));
        }

        /**
         * Retrieve a missing person report by its case number.
         *
         * @param caseNumber the case number that identifies the missing person report
         * @return an ApiResponse containing the matching MissingPerson report and status metadata
         */
        @GetMapping("/missing/number/{caseNumber}")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<ApiResponse<MissingPerson>> getMissingReportByNumber(@PathVariable String caseNumber) {
                return ResponseEntity.ok(missingPersonService.getReportByCaseNumber(caseNumber));
        }

        /**
         * Retrieve status updates for the specified missing person report.
         *
         * @param reportId the identifier of the missing person report
         * @return a list of status updates for the report; empty list if there are none
         */
        @GetMapping("/missing/{reportId}/updates")
        @PreAuthorize("hasRole('USER')")
        public ResponseEntity<List<Update>> getMissingReportUpdates(@PathVariable Long reportId) {
                return ResponseEntity.ok(missingPersonService.getReportUpdates(reportId));
        }
}