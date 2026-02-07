package com.ocrs.backend.repository;

import com.ocrs.backend.entity.FIR;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FIRRepository extends JpaRepository<FIR, Long> {

        /**
 * Retrieves all FIRs associated with the specified user.
 *
 * @param userId the identifier of the user who reported or owns the FIRs
 * @return a list of FIR entities belonging to the given user, empty if none found
 */
List<FIR> findByUserId(Long userId);

        /**
 * Finds FIRs assigned to a specific authority.
 *
 * @param authorityId the identifier of the authority
 * @return a list of FIRs associated with the given authority, or an empty list if none are found
 */
List<FIR> findByAuthorityId(Long authorityId);

        /**
 * Retrieve an FIR by its unique FIR number.
 *
 * @param firNumber the unique FIR identifier to look up
 * @return an Optional containing the matching FIR, or empty if no FIR has that number
 */
Optional<FIR> findByFirNumber(String firNumber);

        /**
 * Retrieves a paginated list of FIRs assigned to the given authority.
 *
 * @param authorityId the identifier of the authority whose FIRs to retrieve
 * @param pageable pagination and sorting information
 * @return a page of FIR entities assigned to the specified authority
 */
Page<FIR> findByAuthorityId(Long authorityId, Pageable pageable);

        /**
 * Retrieve a paginated list of FIRs that have the specified status.
 *
 * @param status the FIR status to filter by
 * @return a page containing FIR entities with the given status
 */
Page<FIR> findByStatus(FIR.Status status, Pageable pageable);

        /**
 * Finds FIRs for a specific category.
 *
 * @param category the FIR category to filter by
 * @param pageable pagination and sorting information
 * @return a page of FIRs matching the specified category
 */
Page<FIR> findByCategory(FIR.Category category, Pageable pageable);

        /**
         * Count FIR records that have the specified status.
         *
         * @param status the FIR status to filter by
         * @return the number of FIRs with the given status
         */
        @Query("SELECT COUNT(f) FROM FIR f WHERE f.status = :status")
        Long countByStatus(FIR.Status status);

        /**
         * Aggregates the number of FIRs for each category.
         *
         * @return a list of two-element Object arrays where the first element is the `FIR.Category` and the second element is the count (`Long`) of FIRs in that category
         */
        @Query("SELECT f.category, COUNT(f) FROM FIR f GROUP BY f.category")
        List<Object[]> countByCategory();

        /**
         * Retrieves the number of FIRs grouped by their status.
         *
         * @return a list of object arrays where each element is a two-entry array: index 0 is the FIR.Status value, index 1 is the corresponding count (Long)
         */
        @Query("SELECT f.status, COUNT(f) FROM FIR f GROUP BY f.status")
        List<Object[]> countGroupByStatus();

        /**
 * Counts FIR records associated with a specific authority.
 *
 * @param authorityId the identifier of the authority whose FIRs are being counted
 * @return the count of FIRs for the given authority
 */
Long countByAuthorityId(Long authorityId);

        /**
         * Counts active FIRs for the specified authority, excluding FIRs with statuses RESOLVED, CLOSED, or REJECTED.
         *
         * @param authorityId the id of the authority
         * @return the number of active FIRs for the authority
         */
        @Query("SELECT COUNT(f) FROM FIR f WHERE f.authorityId = :authorityId " +
                        "AND f.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
        long countActiveByAuthorityId(Long authorityId);

        /**
 * Count FIRs for a specific authority with a given status.
 *
 * @param authorityId the identifier of the authority whose FIRs to count
 * @param status the FIR status to filter by
 * @return the number of FIRs matching the given authority and status
 */
Long countByAuthorityIdAndStatus(Long authorityId, FIR.Status status);

        /**
         * Aggregates FIR counts grouped by status for a specific authority.
         *
         * @param authorityId the ID of the authority to filter FIRs by
         * @return a list of Object[] where index 0 is the FIR status (FIR.Status) and index 1 is the count (Long)
         */
        @Query("SELECT f.status, COUNT(f) FROM FIR f WHERE f.authorityId = :authorityId GROUP BY f.status")
        List<Object[]> countGroupByStatusByAuthority(Long authorityId);

        /**
         * Aggregates the number of FIRs for each assigned authority.
         *
         * @return a list of two-element Object arrays where the first element is the authorityId (Long)
         *         and the second element is the corresponding FIR count (Long)
         */
        @Query("SELECT f.authorityId, COUNT(f) FROM FIR f WHERE f.authorityId IS NOT NULL GROUP BY f.authorityId")
        List<Object[]> countGroupByOfficer();

        /**
                         * Searches FIRs assigned to a specific authority using optional filters.
                         *
                         * <p>Any filter may be null to omit it. The `search` text, if non-null and non-empty,
                         * is matched case-insensitively against the FIR title and FIR number using partial matches.</p>
                         *
                         * @param authorityId the ID of the authority whose FIRs to search
                         * @param search      optional text to match against title or firNumber (case-insensitive, partial)
                         * @param category    optional FIR category to filter by
                         * @param priority    optional FIR priority to filter by
                         * @param status      optional FIR status to filter by
                         * @param pageable    pagination and sorting information for the result page
                         * @return            a page of FIRs matching the authority and the provided filters
                         */
                        @Query("SELECT f FROM FIR f WHERE f.authorityId = :authorityId " +
                        "AND (:search IS NULL OR :search = '' OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(f.firNumber) LIKE LOWER(CONCAT('%', :search, '%'))) "
                        +
                        "AND (:category IS NULL OR f.category = :category) " +
                        "AND (:priority IS NULL OR f.priority = :priority) " +
                        "AND (:status IS NULL OR f.status = :status)")
        Page<FIR> searchByAuthority(Long authorityId, String search, FIR.Category category,
                        FIR.Priority priority, FIR.Status status, Pageable pageable);

        /**
         * Compute the average time in hours between creation and update for FIRs with status RESOLVED.
         *
         * @return the average resolution time in hours, or `null` if no resolved FIRs exist
         */
        @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, updated_at)) FROM firs WHERE status = 'RESOLVED'", nativeQuery = true)
        Double getAverageResolutionTimeInHours();

        /**
 * Count FIRs created after the specified date/time.
 *
 * @param date the exclusive lower bound; only FIRs with createdAt later than this date are counted
 * @return the number of FIRs with createdAt after the provided date
 */
long countByCreatedAtAfter(LocalDateTime date);
}