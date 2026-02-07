package com.ocrs.backend.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// client for external services with async processing and retry support
@Service
public class ExternalServiceClient {

        private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);

        private final WebClient webClient;

        @Value("${services.email-url}")
        private String emailServiceUrl;

        @Value("${services.logging-url}")
        private String loggingServiceUrl;

        /**
         * Constructs an ExternalServiceClient and builds a WebClient configured with a Reactor Netty connector and a 5-second response timeout.
         *
         * @param webClientBuilder builder used to create the WebClient instance for external service calls
         */
        public ExternalServiceClient(WebClient.Builder webClientBuilder) {
                HttpClient httpClient = HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(5));

                this.webClient = webClientBuilder
                                .clientConnector(new ReactorClientHttpConnector(httpClient))
                                .build();
        }

        /**
         * Send an email notification to the configured external email service.
         *
         * @param userId    the ID of the user associated with the notification
         * @param userEmail the recipient email address; if null the payload will omit the email field
         * @param subject   the email subject
         * @param message   the email body
         * @return          a CompletableFuture that completes when the external request completes
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "emailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "emailFallback")
        public CompletableFuture<Void> sendEmailNotification(Long userId, String userEmail, String subject,
                        String message) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null) {
                        payload.put("email", userEmail);
                }
                payload.put("subject", subject);
                payload.put("message", message);
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("email notification sent: {} to {}", subject, userEmail));
        }

        /**
         * Send an FIR-filed email notification for a user.
         *
         * @param userEmail     recipient email address; omitted from the payload if `null`
         * @param authorityName name of the authority handling the FIR; if `null`, defaults to "Pending Assignment"
         * @return              a CompletableFuture that completes when the POST request to the email service finishes
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "firFiledEmailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "firFiledEmailFallback")
        public CompletableFuture<Void> sendFirFiledNotification(Long userId, String userEmail, String firNumber,
                        Long authorityId, String authorityName) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null) {
                        payload.put("email", userEmail);
                }
                payload.put("subject", "FIR Filed Successfully - " + firNumber);
                payload.put("firNumber", firNumber);
                payload.put("authorityId", authorityId);
                payload.put("authorityName", authorityName != null ? authorityName : "Pending Assignment");
                payload.put("template", "firFiled");
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("FIR filed notification sent for {} to user {} ({})", firNumber,
                                                userId,
                                                userEmail));
        }

        /**
         * Send a missing-person report notification to the configured email service.
         *
         * @param userId             the ID of the user who filed the report
         * @param userEmail          recipient email address; omitted from the payload if null
         * @param caseNumber         unique case identifier used in the notification subject
         * @param missingPersonName  name of the missing person
         * @param age                age of the missing person; omitted if null
         * @param gender             gender of the missing person; omitted if null
         * @param height             height description; omitted if null
         * @param complexion         complexion description; omitted if null
         * @param lastSeenDate       date when the person was last seen; omitted if null
         * @param lastSeenLocation   location where the person was last seen; omitted if null
         * @param description        additional descriptive details; omitted if null
         * @param authorityId        identifier of the authority assigned to the case
         * @param authorityName      name of the assigned authority; defaults to "Pending Assignment" when null
         * @param status             case status; defaults to "Pending" when null
         * @return                   a CompletableFuture that completes with no value when the notification request finishes
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "missingPersonFiledEmailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "missingPersonFiledEmailFallback")
        public CompletableFuture<Void> sendMissingPersonFiledNotification(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, Integer age, String gender,
                        String height, String complexion,
                        String lastSeenDate, String lastSeenLocation, String description,
                        Long authorityId, String authorityName, String status) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null) {
                        payload.put("email", userEmail);
                }
                payload.put("subject", "Missing Person Report Filed - " + caseNumber);
                payload.put("caseNumber", caseNumber);
                payload.put("missingPersonName", missingPersonName);
                if (age != null)
                        payload.put("age", age);
                if (gender != null)
                        payload.put("gender", gender);
                if (height != null)
                        payload.put("height", height);
                if (complexion != null)
                        payload.put("complexion", complexion);
                if (lastSeenDate != null)
                        payload.put("lastSeenDate", lastSeenDate);
                if (lastSeenLocation != null)
                        payload.put("lastSeenLocation", lastSeenLocation);
                if (description != null)
                        payload.put("description", description);
                payload.put("authorityId", authorityId);
                payload.put("authorityName", authorityName != null ? authorityName : "Pending Assignment");
                payload.put("status", status != null ? status : "Pending");
                payload.put("template", "missingPersonFiled");
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("Missing person report notification sent for {} to user {} ({})",
                                                caseNumber, userId, userEmail));
        }

        /**
         * Sends a notification about an update to a missing person report.
         *
         * Builds and posts a payload containing the case number, update details, authority information,
         * and optional fields (user email, missing person name, previous status, comment) to the email service.
         *
         * @param userId            the ID of the user related to the report
         * @param userEmail         the recipient email address; if null the email field is omitted
         * @param caseNumber        the missing person case identifier
         * @param missingPersonName the name of the missing person; omitted if null
         * @param updateType        a short label describing the type of update
         * @param newStatus         the report's new status
         * @param previousStatus    the report's prior status; omitted if null
         * @param authorityId       the identifier of the authority handling the case
         * @param authorityName     the name of the authority handling the case
         * @param comment           an optional comment to include in the notification; omitted if null
         * @return a CompletableFuture that completes when the notification request has been submitted
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "missingPersonUpdateEmailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "missingPersonUpdateEmailFallback")
        public CompletableFuture<Void> sendMissingPersonUpdateNotification(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, String updateType,
                        String newStatus, String previousStatus,
                        Long authorityId, String authorityName, String comment) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null)
                        payload.put("email", userEmail);
                payload.put("subject", "Missing Person Report Updated - " + caseNumber);
                payload.put("caseNumber", caseNumber);
                if (missingPersonName != null)
                        payload.put("missingPersonName", missingPersonName);
                payload.put("updateType", updateType);
                payload.put("newStatus", newStatus);
                if (previousStatus != null)
                        payload.put("previousStatus", previousStatus);
                payload.put("authorityId", authorityId);
                payload.put("authorityName", authorityName);
                if (comment != null)
                        payload.put("comment", comment);
                payload.put("template", "missingPersonUpdate");
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("Missing person update notification sent for {} to user {}",
                                                caseNumber, userId));
        }

        /**
         * Send a notification that a missing person report has been reassigned.
         *
         * @param userId               the ID of the user associated with the report
         * @param userEmail            the user's email address; omitted from payload if null
         * @param caseNumber           the missing person report identifier
         * @param missingPersonName    the missing person's name; omitted from payload if null
         * @param status               the current status of the report; omitted from payload if null
         * @param newAuthorityId       the ID of the authority now assigned to the report
         * @param newAuthorityName     the name of the authority now assigned to the report; uses "N/A" if null
         * @param previousAuthorityId  the ID of the previous authority; omitted from payload if null
         * @param previousAuthorityName the name of the previous authority; omitted from payload if null
         * @return                      a CompletableFuture<Void> that completes when the notification POST request finishes
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "missingPersonReassignedEmailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "missingPersonReassignedEmailFallback")
        public CompletableFuture<Void> sendMissingPersonReassignedNotification(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, String status,
                        Long newAuthorityId, String newAuthorityName,
                        Long previousAuthorityId, String previousAuthorityName) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null)
                        payload.put("email", userEmail);
                payload.put("subject", "Missing Person Report Reassigned - " + caseNumber);
                payload.put("caseNumber", caseNumber);
                if (missingPersonName != null)
                        payload.put("missingPersonName", missingPersonName);
                if (status != null)
                        payload.put("status", status);
                payload.put("newAuthorityId", newAuthorityId);
                payload.put("newAuthorityName", newAuthorityName != null ? newAuthorityName : "N/A");
                if (previousAuthorityId != null)
                        payload.put("previousAuthorityId", previousAuthorityId);
                if (previousAuthorityName != null)
                        payload.put("previousAuthorityName", previousAuthorityName);
                payload.put("template", "missingPersonReassigned");
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("Missing person reassignment notification sent for {} to user {}",
                                                caseNumber, userId));
        }

        /**
         * Send an FIR status update notification to the configured email service.
         *
         * Constructs a payload with FIR details, authority information, optional fields, and a timestamp,
         * then posts it to the email notification endpoint.
         *
         * @param userId        the identifier of the user related to the FIR
         * @param userEmail     the user's email address; omitted from the payload if null
         * @param firNumber     the FIR number used in the subject and payload
         * @param updateType    a short label describing the type of update (e.g., "statusChange")
         * @param newStatus     the updated FIR status
         * @param previousStatus the previous FIR status; omitted from the payload if null
         * @param authorityId   the identifier of the authority handling the FIR
         * @param authorityName the name of the authority handling the FIR
         * @param comment       an optional comment about the update; omitted from the payload if null
         * @return a CompletableFuture that completes when the notification request completes
         */
        @Async
        @Retry(name = "emailService", fallbackMethod = "firUpdateEmailFallback")
        @CircuitBreaker(name = "emailService", fallbackMethod = "firUpdateEmailFallback")
        public CompletableFuture<Void> sendFirUpdateNotification(Long userId, String userEmail, String firNumber,
                        String updateType, String newStatus, String previousStatus,
                        Long authorityId, String authorityName, String comment) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                if (userEmail != null)
                        payload.put("email", userEmail);
                payload.put("subject", "FIR Status Updated - " + firNumber);
                payload.put("firNumber", firNumber);
                payload.put("updateType", updateType);
                payload.put("newStatus", newStatus);
                if (previousStatus != null)
                        payload.put("previousStatus", previousStatus);
                payload.put("authorityId", authorityId);
                payload.put("authorityName", authorityName);
                if (comment != null)
                        payload.put("comment", comment);
                payload.put("timestamp", System.currentTimeMillis());

                return sendPostRequest(
                                emailServiceUrl + "/api/notify",
                                payload,
                                () -> logger.info("FIR update notification sent for {} to user {}", firNumber, userId));
        }

        /**
         * Fallback handler invoked when the email service is unavailable; logs a warning and skips sending the notification.
         *
         * @param userId the id of the user for whom the notification was intended
         * @param userEmail the user's email address, if available
         * @param subject the email subject that was being sent
         * @param message the email message that was being sent
         * @param e the exception that triggered the fallback
         * @return a completed CompletableFuture with no result
         */
        public CompletableFuture<Void> emailFallback(Long userId, String userEmail, String subject, String message,
                        Exception e) {
                logger.warn("email service unavailable, skipping notification for user {} ({}): {}", userId, userEmail,
                                subject);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback invoked when the email service is unavailable for a FIR-filed notification.
         *
         * @param userId the identifier of the user for whom the notification was intended
         * @param userEmail the user's email address, if available
         * @param firNumber the FIR number associated with the notification
         * @param authorityId the identifier of the authority assigned to the FIR, if any
         * @param authorityName the name of the authority assigned to the FIR, or null if not assigned
         * @param e the exception that triggered the fallback
         * @return a completed CompletableFuture<Void> that performs no action
         */
        public CompletableFuture<Void> firFiledEmailFallback(Long userId, String userEmail, String firNumber,
                        Long authorityId, String authorityName, Exception e) {
                logger.warn("email service unavailable, skipping FIR filed notification for FIR {} to user {} ({})",
                                firNumber, userId, userEmail);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback invoked when the email service is unavailable for a FIR update notification; logs the skipped notification and performs no further action.
         *
         * @param userId the identifier of the user who would have received the notification
         * @param userEmail the user's email address, if available
         * @param firNumber the FIR number associated with the update
         * @param updateType a short label describing the type of update
         * @param newStatus the FIR's new status
         * @param previousStatus the FIR's previous status, if available
         * @param authorityId the authority identifier associated with the FIR, if any
         * @param authorityName the name of the authority associated with the FIR
         * @param comment an optional comment attached to the update
         * @param e the exception that caused this fallback to be triggered
         * @return a completed CompletableFuture with no result
         */
        public CompletableFuture<Void> firUpdateEmailFallback(Long userId, String userEmail, String firNumber,
                        String updateType, String newStatus, String previousStatus,
                        Long authorityId, String authorityName, String comment, Exception e) {
                logger.warn("email service unavailable, skipping FIR update notification for FIR {} to user {}",
                                firNumber, userId);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback invoked when sending a missing-person filed email cannot be completed.
         *
         * Logs a warning and completes without performing any notification.
         *
         * @param e the exception that triggered the fallback
         * @return a completed CompletableFuture with no result
         */
        public CompletableFuture<Void> missingPersonFiledEmailFallback(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, Integer age, String gender,
                        String height, String complexion,
                        String lastSeenDate, String lastSeenLocation, String description,
                        Long authorityId, String authorityName, String status, Exception e) {
                logger.warn("email service unavailable, skipping missing person notification for case {} to user {} ({})",
                                caseNumber, userId, userEmail);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback handler invoked when the email service is unavailable for a missing-person update notification.
         *
         * @param e the exception that triggered this fallback
         * @return a completed CompletableFuture<Void> that performs no action
         */
        public CompletableFuture<Void> missingPersonUpdateEmailFallback(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, String updateType,
                        String newStatus, String previousStatus,
                        Long authorityId, String authorityName, String comment, Exception e) {
                logger.warn("email service unavailable, skipping missing person update notification for case {} to user {}",
                                caseNumber, userId);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback handler invoked when the email service is unavailable for a missing-person reassignment notification.
         *
         * @param userId                 the id of the user who would have received the notification
         * @param userEmail              the recipient's email address, if available
         * @param caseNumber             the missing-person case identifier
         * @param missingPersonName      the name of the missing person, if available
         * @param status                 the current status associated with the case
         * @param newAuthorityId         the id of the authority to which the case was reassigned
         * @param newAuthorityName       the name of the authority to which the case was reassigned
         * @param previousAuthorityId    the id of the authority previously assigned to the case
         * @param previousAuthorityName  the name of the authority previously assigned to the case
         * @param e                      the exception that triggered the fallback
         * @return                       a completed CompletableFuture<Void> that performs no action
         */
        public CompletableFuture<Void> missingPersonReassignedEmailFallback(
                        Long userId, String userEmail, String caseNumber,
                        String missingPersonName, String status,
                        Long newAuthorityId, String newAuthorityName,
                        Long previousAuthorityId, String previousAuthorityName, Exception e) {
                logger.warn("email service unavailable, skipping missing person reassignment notification for case {} to user {}",
                                caseNumber, userId);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Submit a log event to the external logging service.
         *
         * @param eventType the type or category of the event to record
         * @param userId the identifier of the user associated with the event, or null if not applicable
         * @param reference an optional reference identifier related to the event
         * @return a CompletableFuture that completes with no value when the logging request completes
         */
        @Async
        @Retry(name = "loggingService", fallbackMethod = "logFallback")
        @CircuitBreaker(name = "loggingService", fallbackMethod = "logFallback")
        public CompletableFuture<Void> logEvent(String eventType, Long userId, String reference) {
                return logEvent(eventType, userId, reference, null);
        }

        /**
         * Send a log event to the configured logging service, including an optional detail message.
         *
         * @param eventType a short identifier for the event being logged (e.g., "USER_LOGIN", "FIR_UPDATE")
         * @param userId    the id of the user associated with the event, or null if not applicable
         * @param reference an external reference related to the event (e.g., case number, resource id)
         * @param message   an optional human-readable message or details to include with the log
         * @return          a CompletableFuture that completes when the logging request finishes
         */
        @Async
        @Retry(name = "loggingService", fallbackMethod = "logFallbackWithMessage")
        @CircuitBreaker(name = "loggingService", fallbackMethod = "logFallbackWithMessage")
        public CompletableFuture<Void> logEvent(String eventType, Long userId, String reference, String message) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("eventType", eventType);
                payload.put("userId", userId);
                payload.put("reference", reference);
                payload.put("timestamp", System.currentTimeMillis());
                if (message != null) {
                        payload.put("message", message);
                }

                return sendPostRequest(
                                loggingServiceUrl + "/api/log",
                                payload,
                                () -> logger.debug("event logged: {}", eventType));
        }

        /**
         * Handles logging-service unavailability by emitting a warning and completing without performing the log.
         *
         * @param eventType the type or category of the event that was to be logged
         * @param userId the identifier of the user related to the event
         * @param reference an optional reference string associated with the event (e.g., case number or resource id)
         * @param e the exception that caused the fallback to be invoked
         * @return a completed CompletableFuture with no result
         */
        public CompletableFuture<Void> logFallback(String eventType, Long userId, String reference, Exception e) {
                logger.warn("logging service unavailable, event not logged: {} for user {}", eventType, userId);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Fallback invoked when the logging service is unavailable; logs a warning and completes without performing the remote log.
         *
         * @param eventType the type or category of the event that was being logged
         * @param userId    the identifier of the user associated with the event, may be null
         * @param reference an optional reference string identifying the related entity or operation
         * @param message   an optional descriptive message intended for the log entry
         * @param e         the exception that triggered the fallback
         * @return          a completed CompletableFuture with no result
         */
        public CompletableFuture<Void> logFallbackWithMessage(String eventType, Long userId, String reference,
                        String message, Exception e) {
                logger.warn("logging service unavailable, event not logged: {} for user {} - {}", eventType, userId,
                                message);
                return CompletableFuture.completedFuture(null);
        }

        /**
         * Send a POST request with the given payload and invoke a callback on successful response.
         *
         * @param url the full endpoint URL to post to
         * @param payload the request body as a map of keys to values
         * @param onSuccess a runnable executed when the POST request succeeds
         * @return a CompletableFuture that completes when the request succeeds, or completes exceptionally if the request fails
         */
        private CompletableFuture<Void> sendPostRequest(String url, Map<String, Object> payload, Runnable onSuccess) {
                return webClient.post()
                                .uri(url)
                                .bodyValue(payload)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .doOnSuccess(v -> onSuccess.run())
                                .toFuture();
        }
}