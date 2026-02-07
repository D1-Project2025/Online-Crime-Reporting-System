package com.ocrs.backend.service;

import com.ocrs.backend.client.AuthServiceClient;
import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.dto.MissingPersonRequest;
import com.ocrs.backend.dto.UpdateRequest;
import com.ocrs.backend.dto.UserDTO;
import com.ocrs.backend.entity.MissingPerson;
import com.ocrs.backend.entity.Update;
import com.ocrs.backend.repository.MissingPersonRepository;
import com.ocrs.backend.repository.UpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MissingPersonService {

        private static final Logger logger = LoggerFactory.getLogger(MissingPersonService.class);

        @Autowired
        private MissingPersonRepository missingPersonRepository;

        @Autowired
        private AuthServiceClient authServiceClient;

        @Autowired
        private UpdateRepository updateRepository;

        @Autowired
        private ExternalServiceClient externalServiceClient;

        /**
         * Retrieve a user's email address from the authentication service.
         *
         * @param userId the ID of the user to look up
         * @return the user's email address if found, otherwise {@code null}
         */
        private String getUserEmail(Long userId) {
                if (userId == null) {
                        return null;
                }
                try {
                        ApiResponse<UserDTO> response = authServiceClient.getUserById(userId);
                        if (response.isSuccess() && response.getData() != null) {
                                return response.getData().getEmail();
                        }
                } catch (Exception e) {
                        logger.warn("Failed to fetch user email for ID {}: {}", userId, e.getMessage());
                }
                return null;
        }

        /**
         * Resolve an authority's display name from the Auth service.
         *
         * @param authorityId the authority's ID to resolve
         * @return the authority's full name; returns "Authority #<id>" if the remote lookup fails; returns `null` if `authorityId` is `null`
         */
        private String getAuthorityName(Long authorityId) {
                if (authorityId == null) {
                        return null;
                }
                try {
                        ApiResponse<AuthorityDTO> response = authServiceClient.getAuthorityById(authorityId);
                        if (response.isSuccess() && response.getData() != null) {
                                return response.getData().getFullName();
                        }
                } catch (Exception e) {
                        logger.warn("Failed to fetch authority name for ID {}: {}", authorityId, e.getMessage());
                }
                return "Authority #" + authorityId;
        }

        /**
         * Check whether an authority with the given ID exists.
         *
         * If `authorityId` is null, the method returns `false`. Failures while resolving
         * the authority (for example, communication errors) also result in `false`.
         *
         * @param authorityId the ID of the authority to verify; may be null
         * @return `true` if an authority with the given ID exists, `false` otherwise
         */
        private boolean authorityExists(Long authorityId) {
                if (authorityId == null) {
                        return false;
                }
                try {
                        ApiResponse<AuthorityDTO> response = authServiceClient.getAuthorityById(authorityId);
                        return response.isSuccess() && response.getData() != null;
                } catch (Exception e) {
                        logger.warn("Failed to check authority existence for ID {}: {}", authorityId, e.getMessage());
                        return false;
                }
        }

        /**
         * Create and persist a new missing-person report, optionally auto-assigning it to the least-loaded authority,
         * then notify external services and log the filing event.
         *
         * <p>The created report is initialized with status PENDING and a generated case number. If an authority is
         * available it will be assigned; notifications are sent to the user and an event is logged. On failure an
         * error response is returned.</p>
         *
         * @param userId the identifier of the user filing the report
         * @param request the details of the missing person to record
         * @return an ApiResponse containing the created MissingPerson on success, or an error message on failure
         */
        @Transactional
        public ApiResponse<MissingPerson> fileReport(Long userId, MissingPersonRequest request) {
                try {
                        String caseNumber = "MP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                        // Auto-assign to authority with least active cases
                        Long authorityId = findLeastLoadedAuthority();
                        String authorityName = null;

                        if (authorityId != null) {
                                authorityName = getAuthorityName(authorityId);
                                logger.info("Auto-assigning report {} to authority {} (ID: {})", caseNumber,
                                                authorityName, authorityId);
                        } else {
                                logger.warn("No active authorities available - report {} will be unassigned",
                                                caseNumber);
                        }

                        MissingPerson missingPerson = MissingPerson.builder()
                                        .caseNumber(caseNumber)
                                        .userId(userId)
                                        .authorityId(authorityId)
                                        .missingPersonName(request.getMissingPersonName())
                                        .age(request.getAge())
                                        .gender(request.getGender() != null
                                                        ? MissingPerson.Gender
                                                                        .valueOf(request.getGender().toUpperCase())
                                                        : null)
                                        .height(request.getHeight())
                                        .weight(request.getWeight())
                                        .complexion(request.getComplexion())
                                        .identifyingMarks(request.getIdentifyingMarks())
                                        .lastSeenDate(request.getLastSeenDate())
                                        .lastSeenLocation(request.getLastSeenLocation())
                                        .description(request.getDescription())
                                        .photoUrl(request.getPhotoUrl())
                                        .contactPhone(request.getContactPhone())
                                        .status(MissingPerson.MissingStatus.PENDING)
                                        .build();

                        missingPerson = missingPersonRepository.save(missingPerson);
                        logger.info("Missing person report filed: {} by user {}, assigned to authority: {} ({})",
                                        caseNumber, userId, authorityName, authorityId);

                        // Fetch user's email for notification
                        String userEmail = getUserEmail(userId);
                        // Send detailed missing person notification
                        externalServiceClient.sendMissingPersonFiledNotification(
                                        userId,
                                        userEmail,
                                        caseNumber,
                                        missingPerson.getMissingPersonName(),
                                        missingPerson.getAge(),
                                        missingPerson.getGender() != null ? missingPerson.getGender().name() : null,
                                        missingPerson.getHeight(),
                                        missingPerson.getComplexion(),
                                        missingPerson.getLastSeenDate() != null
                                                        ? missingPerson.getLastSeenDate().toString()
                                                        : null,
                                        missingPerson.getLastSeenLocation(),
                                        missingPerson.getDescription(),
                                        authorityId,
                                        authorityName,
                                        missingPerson.getStatus() != null ? missingPerson.getStatus().name()
                                                        : "PENDING");
                        externalServiceClient.logEvent("MISSING_PERSON_FILED", userId, caseNumber);

                        return ApiResponse.success("Missing person report filed successfully", missingPerson);
                } catch (Exception e) {
                        logger.error("Error filing missing person report: ", e);
                        return ApiResponse.error("Failed to file report: " + e.getMessage());
                }
        }

        /**
         * Selects the active authority with the fewest active missing-person cases.
         *
         * @return the authority id with the fewest active cases, or `null` if no active authorities are available or an error occurs
         */
        private Long findLeastLoadedAuthority() {
                try {
                        ApiResponse<List<AuthorityDTO>> response = authServiceClient.getActiveAuthorities();

                        if (!response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
                                logger.warn("No active authorities found for auto-assignment");
                                return null;
                        }

                        List<AuthorityDTO> activeAuthorities = response.getData();
                        Long leastLoadedAuthority = null;
                        long minCases = Long.MAX_VALUE;

                        for (AuthorityDTO authority : activeAuthorities) {
                                long activeCases = missingPersonRepository.countActiveByAuthorityId(authority.getId());

                                if (activeCases < minCases) {
                                        minCases = activeCases;
                                        leastLoadedAuthority = authority.getId();
                                }
                        }

                        return leastLoadedAuthority;
                } catch (Exception e) {
                        logger.error("Error finding least loaded authority: {}", e.getMessage());
                        return null;
                }
        }

        /**
         * Retrieve all missing person reports filed by the specified user.
         *
         * @param userId the ID of the reporting user
         * @return a list of MissingPerson reports submitted by the given user
         */
        public List<MissingPerson> getReportsByUser(Long userId) {
                return missingPersonRepository.findByUserId(userId);
        }

        /**
         * Retrieve all missing person reports assigned to a specific authority.
         *
         * @param authorityId the authority's database identifier
         * @return a list of MissingPerson entities assigned to the specified authority, or an empty list if none are found
         */
        public List<MissingPerson> getReportsByAuthority(Long authorityId) {
                return missingPersonRepository.findByAuthorityId(authorityId);
        }

        /**
         * Retrieve a paginated list of missing person reports assigned to a specific authority.
         *
         * @param authorityId the ID of the authority whose assigned reports should be returned
         * @param pageable    pagination and sorting information
         * @return a page of MissingPerson reports assigned to the specified authority
         */
        public Page<MissingPerson> getReportsByAuthorityPaged(Long authorityId, Pageable pageable) {
                return missingPersonRepository.findByAuthorityId(authorityId, pageable);
        }

        /**
         * Retrieve a missing-person report by its database ID.
         *
         * @param id the database identifier of the missing-person report
         * @return an ApiResponse containing the report when found, or an error response with message "Report not found"
         */
        public ApiResponse<MissingPerson> getReportById(Long id) {
                return missingPersonRepository.findById(id)
                                .map(mp -> ApiResponse.success("Report found", mp))
                                .orElse(ApiResponse.error("Report not found"));
        }

        /**
         * Retrieve a missing-person report by its case number.
         *
         * @return An ApiResponse containing the matching MissingPerson when found; an error ApiResponse with message "Report not found" otherwise.
         */
        public ApiResponse<MissingPerson> getReportByCaseNumber(String caseNumber) {
                return missingPersonRepository.findByCaseNumber(caseNumber)
                                .map(mp -> ApiResponse.success("Report found", mp))
                                .orElse(ApiResponse.error("Report not found"));
        }

        /**
         * Update the status of a missing-person report and record the corresponding update.
         *
         * <p>Validates the report exists and that the caller (authorityId) is authorized to modify it,
         * prevents modifications when the report is CLOSED, applies a new status from the request when present,
         * records an Update entry, notifies the report owner, and logs the change.</p>
         *
         * @param reportId    the database ID of the missing-person report to update
         * @param authorityId the authority performing the update
         * @param request     details of the update (may include a new status, update type, and comment)
         * @return            an ApiResponse containing the updated MissingPerson on success, or an error message when
         *                    the report is not found, the caller is not authorized, or the report cannot be modified
         */
        @Transactional
        public ApiResponse<MissingPerson> updateReportStatus(Long reportId, Long authorityId, UpdateRequest request) {
                MissingPerson report = missingPersonRepository.findById(reportId).orElse(null);
                if (report == null) {
                        return ApiResponse.error("Report not found");
                }

                if (report.getAuthorityId() == null || !report.getAuthorityId().equals(authorityId)) {
                        return ApiResponse.error("You are not authorized to update this report");
                }

                // Prevent updates on closed cases - closed cases are final
                if (report.getStatus() == MissingPerson.MissingStatus.CLOSED) {
                        logger.warn("Rejected update attempt on closed report {} by authority {}",
                                        report.getCaseNumber(), authorityId);
                        return ApiResponse.error(
                                        "Cannot update a closed case. Closed cases are final and cannot be modified.");
                }

                // Get authority details for notifications via Feign
                String authorityName = getAuthorityName(authorityId);

                String previousStatus = report.getStatus().name();

                if (request.getNewStatus() != null) {
                        report.setStatus(MissingPerson.MissingStatus.valueOf(request.getNewStatus().toUpperCase()));
                }

                report = missingPersonRepository.save(report);

                Update update = Update.builder()
                                .missingPersonId(reportId)
                                .authorityId(authorityId)
                                .updateType(Update.UpdateType.valueOf(request.getUpdateType().toUpperCase()))
                                .previousStatus(previousStatus)
                                .newStatus(report.getStatus().name())
                                .comment(request.getComment())
                                .build();

                updateRepository.save(update);

                // Send detailed update notification to user
                String userEmail = getUserEmail(report.getUserId());
                externalServiceClient.sendMissingPersonUpdateNotification(
                                report.getUserId(),
                                userEmail,
                                report.getCaseNumber(),
                                report.getMissingPersonName(),
                                request.getUpdateType(),
                                report.getStatus().name(),
                                previousStatus,
                                authorityId,
                                authorityName,
                                request.getComment());

                // Log the update event with detailed message
                String logMessage = String.format("Case: %s, Authority: %s (ID: %d), Update: %s, Status: %s -> %s",
                                report.getCaseNumber(), authorityName, authorityId,
                                request.getUpdateType(), previousStatus, report.getStatus().name());
                externalServiceClient.logEvent("MISSING_PERSON_UPDATED", authorityId, report.getCaseNumber(),
                                logMessage);

                logger.info("Missing person report {} updated by authority {} ({})",
                                report.getCaseNumber(), authorityName, authorityId);
                return ApiResponse.success("Report updated successfully", report);
        }

        /**
         * Retrieve updates for a specific missing person report in reverse chronological order.
         *
         * @param reportId the database ID of the missing person report
         * @return a list of Update entities for the report ordered by creation time descending
         */
        public List<Update> getReportUpdates(Long reportId) {
                return updateRepository.findByMissingPersonIdOrderByCreatedAtDesc(reportId);
        }

        /**
         * Searches missing-person reports assigned to a specific authority using optional text and status filters and returns paginated results.
         *
         * @param authorityId the authority's database id to filter reports by; may be null to ignore authority filtering
         * @param search      optional text to match against report fields (e.g., name, case number); may be null or empty
         * @param status      optional status to filter reports by; may be null to include all statuses
         * @param pageable    pagination and sorting information
         * @return            a page of MissingPerson reports that match the provided filters
         */
        public Page<MissingPerson> searchReportsByAuthority(Long authorityId, String search,
                        MissingPerson.MissingStatus status, Pageable pageable) {
                return missingPersonRepository.searchByAuthority(authorityId, search, status, pageable);
        }

        /**
         * Retrieve all missing person reports.
         *
         * @return a list containing every stored MissingPerson report
         */
        public List<MissingPerson> getAllReports() {
                return missingPersonRepository.findAll();
        }

        /**
         * Reassigns a missing-person report to a different authority, records the reassignment, and notifies interested parties.
         *
         * <p>This updates the report's assigned authority, creates an Update entry describing the reassignment, sends a
         * notification to the report owner and external systems, and emits an event for audit/logging.</p>
         *
         * @param reportId      the database ID of the missing-person report to reassign
         * @param newAuthorityId the ID of the authority to assign the report to
         * @return               an ApiResponse containing the updated MissingPerson on success; an error ApiResponse if the
         *                       report does not exist, the new authority does not exist, or the report is already assigned to
         *                       the specified authority
         */
        @Transactional
        public ApiResponse<MissingPerson> reassignReport(Long reportId, Long newAuthorityId) {
                MissingPerson report = missingPersonRepository.findById(reportId).orElse(null);
                if (report == null) {
                        return ApiResponse.error("Report not found");
                }

                // Verify authority exists via Feign
                if (!authorityExists(newAuthorityId)) {
                        return ApiResponse.error("Authority not found");
                }

                if (report.getAuthorityId() != null && report.getAuthorityId().equals(newAuthorityId)) {
                        return ApiResponse.error("Cannot reassign to the same authority");
                }

                // Get authority name via Feign
                String authorityName = getAuthorityName(newAuthorityId);

                Long previousAuthorityId = report.getAuthorityId();
                report.setAuthorityId(newAuthorityId);
                report = missingPersonRepository.save(report);

                // Create update record
                Update update = Update.builder()
                                .missingPersonId(reportId)
                                .authorityId(newAuthorityId)
                                .updateType(Update.UpdateType.REASSIGNMENT)
                                .comment("Reassigned from authority " + previousAuthorityId + " to " + newAuthorityId)
                                .build();

                updateRepository.save(update);

                // Fetch user's email and previous authority name for notification
                String userEmail = getUserEmail(report.getUserId());
                String previousAuthorityName = previousAuthorityId != null ? getAuthorityName(previousAuthorityId)
                                : null;

                externalServiceClient.sendMissingPersonReassignedNotification(
                                report.getUserId(),
                                userEmail,
                                report.getCaseNumber(),
                                report.getMissingPersonName(),
                                report.getStatus() != null ? report.getStatus().name() : "PENDING",
                                newAuthorityId,
                                authorityName,
                                previousAuthorityId,
                                previousAuthorityName);

                // Log the reassignment event
                externalServiceClient.logEvent("MISSING_PERSON_REASSIGNED", newAuthorityId, report.getCaseNumber(),
                                "Report reassigned from authority " + previousAuthorityId + " to " + newAuthorityId);

                logger.info("Missing person report {} reassigned to authority {}", report.getCaseNumber(),
                                newAuthorityId);
                return ApiResponse.success("Report reassigned successfully", report);
        }
}