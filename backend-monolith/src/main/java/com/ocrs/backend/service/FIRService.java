package com.ocrs.backend.service;

import com.ocrs.backend.client.AuthServiceClient;
import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.dto.FIRRequest;
import com.ocrs.backend.dto.UpdateRequest;
import com.ocrs.backend.dto.UserDTO;
import com.ocrs.backend.entity.FIR;
import com.ocrs.backend.entity.Update;
import com.ocrs.backend.repository.FIRRepository;
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
public class FIRService {

        private static final Logger logger = LoggerFactory.getLogger(FIRService.class);

        @Autowired
        private FIRRepository firRepository;

        @Autowired
        private AuthServiceClient authServiceClient;

        @Autowired
        private UpdateRepository updateRepository;

        @Autowired
        private ExternalServiceClient externalServiceClient;

        /**
         * Retrieve the email address for a user by their user ID.
         *
         * @param userId the ID of the user to look up
         * @return the user's email if found; `null` if the userId is null, the user is not found, or an error occurs while fetching
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
         * Retrieve the full name of an authority from the Auth service.
         *
         * Attempts to fetch the authority's full name; if `authorityId` is null returns `null`,
         * and if the remote lookup fails or yields no data returns the fallback string "Authority #<id>".
         *
         * @param authorityId the authority's identifier
         * @return the authority's full name, `"Authority #<id>"` on lookup failure, or `null` if `authorityId` is null
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
         * Determine whether an authority with the given ID exists.
         *
         * @param authorityId the authority's ID; if null the method returns `false`
         * @return `true` if an authority with the given ID exists, `false` otherwise (returns `false` on remote call failure)
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
         * Determine FIR priority from the crime category using a fixed rule-based mapping.
         *
         * This enforces server-side priority assignment and overrides any user-selected priority
         * to avoid bias toward higher urgency selections.
         *
         * @param category the crime category to evaluate
         * @return the corresponding FIR.Priority for the provided category (ASSAULT → URGENT; HARASSMENT, CYBERCRIME → HIGH; FRAUD, THEFT, OTHER → MEDIUM; VANDALISM → LOW)
         */
        private FIR.Priority determinePriority(FIR.Category category) {
                return switch (category) {
                        case ASSAULT -> FIR.Priority.URGENT; // physical violence - immediate attention
                        case HARASSMENT -> FIR.Priority.HIGH; // personal safety concern
                        case CYBERCRIME -> FIR.Priority.HIGH; // time-sensitive (evidence can be deleted)
                        case FRAUD -> FIR.Priority.MEDIUM; // financial crime, less immediate
                        case THEFT -> FIR.Priority.MEDIUM; // property crime
                        case VANDALISM -> FIR.Priority.LOW; // property damage, non-violent
                        case OTHER -> FIR.Priority.MEDIUM; // default for unclassified
                };
        }

        /**
         * Create and persist a new FIR, auto-assign it to the least-loaded authority, determine priority from the category, and notify external services about the filed FIR.
         *
         * The created FIR will have a generated FIR number and a status of PENDING. If an authority is available the FIR will be assigned to it; otherwise it will remain unassigned. On failure this method returns an error ApiResponse containing a descriptive message.
         *
         * @param userId the ID of the user filing the FIR
         * @param request the FIRRequest containing title, description, category, incident details, and evidence URLs
         * @return an ApiResponse containing the created FIR on success, or an error ApiResponse with a descriptive message on failure
         */
        @Transactional
        public ApiResponse<FIR> fileFIR(Long userId, FIRRequest request) {
                try {
                        // generate unique FIR number
                        String firNumber = "FIR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                        // auto-assign to authority with least active cases
                        Long authorityId = findLeastLoadedAuthority();
                        String authorityName = null;

                        if (authorityId != null) {
                                authorityName = getAuthorityName(authorityId);
                                logger.info("Auto-assigning FIR {} to authority {} (ID: {})", firNumber, authorityName,
                                                authorityId);
                        } else {
                                logger.warn("No active authorities available - FIR {} will be unassigned", firNumber);
                        }

                        // determine priority based on category (automatic assignment)
                        FIR.Category category = FIR.Category.valueOf(request.getCategory().toUpperCase());
                        FIR.Priority autoPriority = determinePriority(category);
                        logger.info("Auto-assigned priority {} for category {} in FIR {}", autoPriority, category,
                                        firNumber);

                        FIR fir = FIR.builder()
                                        .firNumber(firNumber)
                                        .userId(userId)
                                        .authorityId(authorityId)
                                        .category(category)
                                        .title(request.getTitle())
                                        .description(request.getDescription())
                                        .incidentDate(request.getIncidentDate())
                                        .incidentTime(request.getIncidentTime())
                                        .incidentLocation(request.getIncidentLocation())
                                        .status(FIR.Status.PENDING)
                                        .priority(autoPriority)
                                        .evidenceUrls(request.getEvidenceUrls())
                                        .build();

                        fir = firRepository.save(fir);
                        logger.info("FIR filed: {} by user {}, assigned to authority: {} ({})", firNumber, userId,
                                        authorityName, authorityId);

                        // notify external services with FIR number and authority details
                        // fetch user's email for notification
                        String userEmail = getUserEmail(userId);
                        externalServiceClient.sendFirFiledNotification(userId, userEmail, firNumber, authorityId,
                                        authorityName);
                        externalServiceClient.logEvent("FIR_FILED", userId, firNumber);

                        return ApiResponse.success("FIR filed successfully", fir);
                } catch (Exception e) {
                        logger.error("Error filing FIR: ", e);
                        return ApiResponse.error("Failed to file FIR: " + e.getMessage());
                }
        }

        /**
         * Selects the authority with the fewest active (non-closed) FIR cases for auto-assignment.
         *
         * @return the authority ID with the fewest active cases, or `null` if no active authorities are available or an error occurs
         */
        private Long findLeastLoadedAuthority() {
                try {
                        // get all active authorities from Auth service via Feign
                        ApiResponse<List<AuthorityDTO>> response = authServiceClient.getActiveAuthorities();

                        if (!response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
                                logger.warn("No active authorities found for auto-assignment");
                                return null;
                        }

                        List<AuthorityDTO> activeAuthorities = response.getData();

                        // determine authority with minimum active cases
                        Long leastLoadedAuthority = null;
                        long minCases = Long.MAX_VALUE;

                        for (AuthorityDTO authority : activeAuthorities) {
                                // count active (non-closed, non-resolved) FIRs for this authority
                                long activeCases = firRepository.countActiveByAuthorityId(authority.getId());

                                logger.debug("Authority {} (ID: {}) has {} active cases",
                                                authority.getFullName(), authority.getId(), activeCases);

                                if (activeCases < minCases) {
                                        minCases = activeCases;
                                        leastLoadedAuthority = authority.getId();
                                }
                        }

                        logger.info("Least loaded authority ID: {} with {} active cases", leastLoadedAuthority,
                                        minCases);
                        return leastLoadedAuthority;

                } catch (Exception e) {
                        logger.error("Error finding least loaded authority: {}", e.getMessage());
                        return null;
                }
        }

        /**
         * Fetches all FIR records filed by the specified user.
         *
         * @param userId the identifier of the user whose FIRs are requested
         * @return a list of FIRs filed by the user; an empty list if none are found
         */
        public List<FIR> getFIRsByUser(Long userId) {
                return firRepository.findByUserId(userId);
        }

        /**
         * Retrieve all FIR records assigned to a specific authority.
         *
         * @param authorityId the authority's identifier to filter FIRs by
         * @return a list of FIRs assigned to the specified authority; empty list if none are found
         */
        public List<FIR> getFIRsByAuthority(Long authorityId) {
                return firRepository.findByAuthorityId(authorityId);
        }

        /**
         * Retrieve a page of FIRs assigned to a specific authority.
         *
         * @param authorityId the ID of the authority whose FIRs to retrieve
         * @param pageable    pagination and sorting parameters
         * @return            a page of FIRs assigned to the given authority (empty if none)
         */
        public Page<FIR> getFIRsByAuthorityPaged(Long authorityId, Pageable pageable) {
                return firRepository.findByAuthorityId(authorityId, pageable);
        }

        /**
         * Retrieve a FIR by its database identifier.
         *
         * @param id the primary key of the FIR to fetch
         * @return an ApiResponse containing the FIR when found; an error ApiResponse with message "FIR not found" otherwise
         */
        public ApiResponse<FIR> getFIRById(Long id) {
                return firRepository.findById(id)
                                .map(fir -> ApiResponse.success("FIR found", fir))
                                .orElse(ApiResponse.error("FIR not found"));
        }

        /**
         * Retrieve a FIR by its FIR number.
         *
         * @param firNumber the FIR number to search for
         * @return an ApiResponse containing the FIR when found; otherwise an error ApiResponse with message "FIR not found"
         */
        public ApiResponse<FIR> getFIRByNumber(String firNumber) {
                return firRepository.findByFirNumber(firNumber)
                                .map(fir -> ApiResponse.success("FIR found", fir))
                                .orElse(ApiResponse.error("FIR not found"));
        }

        /**
         * Update the status of a FIR and record the change as an Update performed by the assigned authority.
         *
         * Creates and persists an Update record, notifies the FIR owner, and logs an event when the update is applied.
         *
         * @param firId      the database ID of the FIR to update
         * @param authorityId the ID of the authority performing the update; must match the FIR's assigned authority
         * @param request    the update payload containing the new status, update type, and an optional comment
         * @return           an ApiResponse containing the updated FIR on success; an error ApiResponse with a descriptive message if the FIR is not found, the authority is not authorized, or the FIR is closed
         */
        @Transactional
        public ApiResponse<FIR> updateFIRStatus(Long firId, Long authorityId, UpdateRequest request) {
                FIR fir = firRepository.findById(firId).orElse(null);
                if (fir == null) {
                        return ApiResponse.error("FIR not found");
                }

                // verify authority is assigned to this FIR
                if (fir.getAuthorityId() == null || !fir.getAuthorityId().equals(authorityId)) {
                        return ApiResponse.error("You are not authorized to update this FIR");
                }

                // prevent updates on closed cases - closed cases are final
                if (fir.getStatus() == FIR.Status.CLOSED) {
                        logger.warn("Rejected update attempt on closed FIR {} by authority {}",
                                        fir.getFirNumber(), authorityId);
                        return ApiResponse.error(
                                        "Cannot update a closed case. Closed cases are final and cannot be modified.");
                }

                // get authority details for notifications via Feign
                String authorityName = getAuthorityName(authorityId);

                String previousStatus = fir.getStatus().name();

                if (request.getNewStatus() != null) {
                        fir.setStatus(FIR.Status.valueOf(request.getNewStatus().toUpperCase()));
                }

                fir = firRepository.save(fir);

                // create update record
                Update update = Update.builder()
                                .firId(firId)
                                .authorityId(authorityId)
                                .updateType(Update.UpdateType.valueOf(request.getUpdateType().toUpperCase()))
                                .previousStatus(previousStatus)
                                .newStatus(fir.getStatus().name())
                                .comment(request.getComment())
                                .build();

                updateRepository.save(update);

                // send detailed FIR update notification to user
                // fetch user's email for notification
                String userEmail = getUserEmail(fir.getUserId());
                externalServiceClient.sendFirUpdateNotification(
                                fir.getUserId(),
                                userEmail,
                                fir.getFirNumber(),
                                request.getUpdateType(),
                                fir.getStatus().name(),
                                previousStatus,
                                authorityId,
                                authorityName,
                                request.getComment());

                // log the update event with detailed message
                String logMessage = String.format("FIR: %s, Authority: %s (ID: %d), Update: %s, Status: %s -> %s",
                                fir.getFirNumber(), authorityName, authorityId,
                                request.getUpdateType(), previousStatus, fir.getStatus().name());
                externalServiceClient.logEvent("FIR_UPDATED", authorityId, fir.getFirNumber(), logMessage);

                logger.info("FIR {} updated by authority {} ({})", fir.getFirNumber(), authorityName, authorityId);
                return ApiResponse.success("FIR updated successfully", fir);
        }

        /**
         * Retrieves the update history for the specified FIR, ordered from newest to oldest.
         *
         * @param firId the FIR identifier whose updates to retrieve
         * @return a list of Update records for the FIR ordered by `createdAt` descending
         */
        public List<Update> getFIRUpdates(Long firId) {
                return updateRepository.findByFirIdOrderByCreatedAtDesc(firId);
        }

        /**
         * Searches FIRs assigned to the given authority using optional text and enum filters, returning a paged result.
         *
         * @param authorityId the authority's ID to limit the search to
         * @param search      optional text to match against FIR fields such as title or description
         * @param category    optional category filter
         * @param priority    optional priority filter
         * @param status      optional status filter
         * @param pageable    pagination and sorting information
         * @return            a page of FIRs that match the provided filters
         */
        public Page<FIR> searchFIRsByAuthority(Long authorityId, String search, FIR.Category category,
                        FIR.Priority priority, FIR.Status status, Pageable pageable) {
                return firRepository.searchByAuthority(authorityId, search, category, priority, status, pageable);
        }

        /**
         * Retrieve all FIR records.
         *
         * @return a list of all FIR entities, or an empty list if none exist
         */
        public List<FIR> getAllFIRs() {
                return firRepository.findAll();
        }

        /**
         * Reassigns an existing FIR to a different authority, records the reassignment as an Update,
         * notifies the FIR's user by email, and logs the reassignment event.
         *
         * This operation validates the FIR and the target authority, prevents reassignment to the
         * same authority, updates and persists the FIR's authority, creates an Update record describing
         * the reassignment, sends an email notification to the FIR owner, and emits a log event.
         *
         * @param firId         the database identifier of the FIR to reassign
         * @param newAuthorityId the identifier of the authority to assign the FIR to
         * @return               an ApiResponse containing the updated FIR on success, or an error message on failure
         */
        @Transactional
        public ApiResponse<FIR> reassignFIR(Long firId, Long newAuthorityId) {
                FIR fir = firRepository.findById(firId).orElse(null);
                if (fir == null) {
                        return ApiResponse.error("FIR not found");
                }

                // verify authority exists via Feign
                if (!authorityExists(newAuthorityId)) {
                        return ApiResponse.error("Authority not found");
                }

                if (fir.getAuthorityId() != null && fir.getAuthorityId().equals(newAuthorityId)) {
                        return ApiResponse.error("Cannot reassign to the same authority");
                }

                // get authority name via Feign
                String authorityName = getAuthorityName(newAuthorityId);

                Long previousAuthorityId = fir.getAuthorityId();
                fir.setAuthorityId(newAuthorityId);
                fir = firRepository.save(fir);

                // Create update record
                Update update = Update.builder()
                                .firId(firId)
                                .authorityId(newAuthorityId)
                                .updateType(Update.UpdateType.REASSIGNMENT)
                                .comment("Reassigned from authority " + previousAuthorityId + " to " + newAuthorityId)
                                .build();

                updateRepository.save(update);

                // send email notification to user about reassignment
                String userEmail = getUserEmail(fir.getUserId());
                externalServiceClient.sendEmailNotification(fir.getUserId(), userEmail, "FIR Reassigned",
                                "Your FIR " + fir.getFirNumber() + " has been reassigned to a new officer: "
                                                + authorityName);

                // log the reassignment event
                externalServiceClient.logEvent("FIR_REASSIGNED", newAuthorityId, fir.getFirNumber(),
                                "FIR reassigned from authority " + previousAuthorityId + " to " + newAuthorityId);

                logger.info("FIR {} reassigned to authority {}", fir.getFirNumber(), newAuthorityId);
                return ApiResponse.success("FIR reassigned successfully", fir);
        }
}