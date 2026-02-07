package com.ocrs.backend.service;

import com.ocrs.backend.client.AuthServiceClient;
import com.ocrs.backend.dto.AuthorityAnalyticsResponse;
import com.ocrs.backend.dto.AnalyticsResponse;
import com.ocrs.backend.dto.ApiResponse;
import com.ocrs.backend.dto.AuthorityDTO;
import com.ocrs.backend.entity.FIR;
import com.ocrs.backend.entity.MissingPerson;
import com.ocrs.backend.repository.FIRRepository;
import com.ocrs.backend.repository.MissingPersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

        private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

        @Autowired
        private FIRRepository firRepository;

        @Autowired
        private MissingPersonRepository missingPersonRepository;

        @Autowired
        private AuthServiceClient authServiceClient;

        /**
         * Assembles overall analytics for FIRs, missing persons, and authorities.
         *
         * @return an AnalyticsResponse containing aggregated metrics:
         *         total, pending, and resolved FIR counts; total and found missing person counts;
         *         active authority count; FIRs grouped by category and by status; missing persons grouped by status;
         *         top authorities mapped to their FIR counts (authority names resolved); average resolution time in days
         *         (rounded to one decimal); and FIR growth rate as a percentage comparing the last 30 days to the previous 30 days
         *         (rounded to one decimal).
         */
        public AnalyticsResponse getAnalytics() {
                // FIR counts
                Long totalFirs = firRepository.count();
                Long pendingFirs = firRepository.countByStatus(FIR.Status.PENDING);
                Long resolvedFirs = firRepository.countByStatus(FIR.Status.RESOLVED);

                // Missing person counts
                Long totalMissingPersons = missingPersonRepository.count();
                Long foundPersons = missingPersonRepository.countByStatus(MissingPerson.MissingStatus.FOUND);

                // Authority count - fetch from Auth service via Feign
                Long totalAuthorities = getActiveAuthorityCount();

                // FIRs by category
                Map<String, Long> firsByCategory = new HashMap<>();
                List<Object[]> categoryData = firRepository.countByCategory();
                for (Object[] row : categoryData) {
                        firsByCategory.put(row[0].toString(), (Long) row[1]);
                }

                // FIRs by status
                Map<String, Long> firsByStatus = new HashMap<>();
                List<Object[]> statusData = firRepository.countGroupByStatus();
                for (Object[] row : statusData) {
                        firsByStatus.put(row[0].toString(), (Long) row[1]);
                }

                // Missing persons by status
                Map<String, Long> missingByStatus = new HashMap<>();
                List<Object[]> missingStatusData = missingPersonRepository.countGroupByStatus();
                for (Object[] row : missingStatusData) {
                        missingByStatus.put(row[0].toString(), (Long) row[1]);
                }

                // Top authorities by case count (FIRs) - resolve authority IDs to names
                Map<String, Long> topAuthorities = new HashMap<>();
                List<Object[]> officerData = firRepository.countGroupByOfficer();
                for (Object[] row : officerData) {
                        if (row[0] != null) {
                                Long authorityId = (Long) row[0];
                                Long count = (Long) row[1];
                                String officerName = getAuthorityName(authorityId);
                                topAuthorities.put(officerName, count);
                        }
                }

                // Calculate Average Resolution Time
                Double avgResTimeHours = firRepository.getAverageResolutionTimeInHours();
                // Convert to days, default to 0.0
                Double averageResolutionTime = avgResTimeHours != null ? avgResTimeHours / 24.0 : 0.0;
                // Round to 1 decimal place
                averageResolutionTime = Math.round(averageResolutionTime * 10.0) / 10.0;

                // Calculate FIR Growth Rate (Last 30 days vs Previous 30 days)
                LocalDateTime now = LocalDateTime.now();
                long last30Days = firRepository.countByCreatedAtAfter(now.minusDays(30));
                long last60Days = firRepository.countByCreatedAtAfter(now.minusDays(60));
                long previous30Days = last60Days - last30Days;

                Double firGrowthRate = 0.0;
                if (previous30Days > 0) {
                        firGrowthRate = ((double) (last30Days - previous30Days) / previous30Days) * 100;
                } else if (last30Days > 0) {
                        firGrowthRate = 100.0; // 100% growth if started from 0
                }
                firGrowthRate = Math.round(firGrowthRate * 10.0) / 10.0;

                return AnalyticsResponse.builder()
                                .totalFirs(totalFirs)
                                .pendingFirs(pendingFirs)
                                .resolvedFirs(resolvedFirs)
                                .totalMissingPersons(totalMissingPersons)
                                .foundPersons(foundPersons)
                                .totalAuthorities(totalAuthorities)
                                .firsByCategory(firsByCategory)
                                .firsByStatus(firsByStatus)
                                .missingByStatus(missingByStatus)
                                .topAuthorities(topAuthorities)
                                .averageResolutionTime(averageResolutionTime)
                                .firGrowthRate(firGrowthRate)
                                .build();
        }

        /**
         * Retrieve the number of active authorities from the Auth service.
         *
         * @return the count of active authorities, or 0 if the Auth service call fails or returns no data
         */
        private Long getActiveAuthorityCount() {
                try {
                        ApiResponse<List<AuthorityDTO>> response = authServiceClient.getActiveAuthorities();
                        if (response.isSuccess() && response.getData() != null) {
                                return (long) response.getData().size();
                        }
                } catch (Exception e) {
                        logger.warn("Failed to get authority count from Auth service: {}", e.getMessage());
                }
                return 0L;
        }

        /**
         * Resolve an authority's full name by ID using the Auth service.
         *
         * @param authorityId the ID of the authority to look up
         * @return the authority's full name if available, or "Officer #<id>" if not found or on error
         */
        private String getAuthorityName(Long authorityId) {
                try {
                        ApiResponse<AuthorityDTO> response = authServiceClient.getAuthorityById(authorityId);
                        if (response.isSuccess() && response.getData() != null) {
                                return response.getData().getFullName();
                        }
                } catch (Exception e) {
                        logger.warn("Failed to get authority name for ID {}: {}", authorityId, e.getMessage());
                }
                return "Officer #" + authorityId;
        }

        /**
         * Builds analytics for a specific authority identified by its ID.
         *
         * @param authorityId the authority's database identifier
         * @return an AuthorityAnalyticsResponse containing:
         *         - assignedFIRs: total FIRs assigned to the authority
         *         - pendingFIRs: assigned FIRs with status PENDING
         *         - resolvedFIRs: assigned FIRs with status RESOLVED
         *         - assignedMissingReports: total missing-person reports assigned to the authority
         *         - foundMissingReports: assigned missing-person reports with status FOUND
         *         - firsByStatus: map of FIR status to counts for this authority
         *         - missingByStatus: map of missing-person status to counts for this authority
         */
        public AuthorityAnalyticsResponse getAuthorityAnalytics(Long authorityId) {
                Long assignedFIRs = firRepository.countByAuthorityId(authorityId);
                Long pendingFIRs = firRepository.countByAuthorityIdAndStatus(authorityId, FIR.Status.PENDING);
                Long resolvedFIRs = firRepository.countByAuthorityIdAndStatus(authorityId, FIR.Status.RESOLVED);

                Long assignedMissing = missingPersonRepository.countByAuthorityId(authorityId);
                Long foundMissing = missingPersonRepository.countByAuthorityIdAndStatus(authorityId,
                                MissingPerson.MissingStatus.FOUND);

                Map<String, Long> firsByStatus = new HashMap<>();
                for (Object[] row : firRepository.countGroupByStatusByAuthority(authorityId)) {
                        firsByStatus.put(row[0].toString(), (Long) row[1]);
                }

                Map<String, Long> missingByStatus = new HashMap<>();
                for (Object[] row : missingPersonRepository.countGroupByStatusByAuthority(authorityId)) {
                        missingByStatus.put(row[0].toString(), (Long) row[1]);
                }

                return AuthorityAnalyticsResponse.builder()
                                .assignedFIRs(assignedFIRs)
                                .pendingFIRs(pendingFIRs)
                                .resolvedFIRs(resolvedFIRs)
                                .assignedMissingReports(assignedMissing)
                                .foundMissingReports(foundMissing)
                                .firsByStatus(firsByStatus)
                                .missingByStatus(missingByStatus)
                                .build();
        }
}