package com.ocrs.backend.repository;

import com.ocrs.backend.entity.MissingPerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissingPersonRepository extends JpaRepository<MissingPerson, Long> {

        /**
 * Finds all MissingPerson records associated with the given user.
 *
 * @param userId the id of the user whose missing person records are being queried
 * @return a list of MissingPerson entities associated with the specified user
 */
List<MissingPerson> findByUserId(Long userId);

        /**
 * Finds all missing person records for a given authority.
 *
 * @param authorityId the identifier of the authority to filter records by
 * @return a list of MissingPerson entities associated with the specified authority, possibly empty
 */
List<MissingPerson> findByAuthorityId(Long authorityId);

        /**
 * Finds a missing person record by its case number.
 *
 * @param caseNumber the case number identifier to look up
 * @return an Optional containing the MissingPerson with the specified case number, or Optional.empty() if none is found
 */
Optional<MissingPerson> findByCaseNumber(String caseNumber);

        /**
 * Retrieves a paged list of MissingPerson records associated with the given authority.
 *
 * @param authorityId the identifier of the authority whose records to retrieve
 * @param pageable pagination and sorting information
 * @return a page of MissingPerson entities for the specified authority
 */
Page<MissingPerson> findByAuthorityId(Long authorityId, Pageable pageable);

        /**
 * Retrieve a page of missing person records that have the specified status.
 *
 * @param status   the status to filter missing person records by
 * @param pageable paging and sorting information for the result set
 * @return         a page of MissingPerson entities matching the given status
 */
Page<MissingPerson> findByStatus(MissingPerson.MissingStatus status, Pageable pageable);

        /**
         * Count MissingPerson records that match the specified status.
         *
         * @param status the status to match
         * @return the number of records with the given status
         */
        @Query("SELECT COUNT(m) FROM MissingPerson m WHERE m.status = :status")
        Long countByStatus(MissingPerson.MissingStatus status);

        /**
         * Aggregate missing-person records grouped by their status.
         *
         * @return a list of Object[] where index 0 is the MissingPerson.MissingStatus and index 1 is the corresponding count as a Long
         */
        @Query("SELECT m.status, COUNT(m) FROM MissingPerson m GROUP BY m.status")
        List<Object[]> countGroupByStatus();

        /**
 * Count missing person records associated with the specified authority.
 *
 * @param authorityId the authority's identifier to count records for
 * @return the number of MissingPerson records linked to the given authorityId
 */
Long countByAuthorityId(Long authorityId);

        /**
         * Count active missing-person reports for the specified authority.
         *
         * Active reports are those whose status is neither 'FOUND' nor 'CLOSED'.
         *
         * @param authorityId the authority's identifier to filter reports by
         * @return the number of active reports for the given authority
         */
        @Query("SELECT COUNT(m) FROM MissingPerson m WHERE m.authorityId = :authorityId " +
                        "AND m.status NOT IN ('FOUND', 'CLOSED')")
        long countActiveByAuthorityId(Long authorityId);

        /**
 * Counts missing-person records belonging to the given authority that have the specified status.
 *
 * @param authorityId the identifier of the authority to filter by
 * @param status      the status to filter records by
 * @return            the count of matching MissingPerson records
 */
Long countByAuthorityIdAndStatus(Long authorityId, MissingPerson.MissingStatus status);

        /**
         * Retrieve counts of missing-person records grouped by status for a specific authority.
         *
         * @param authorityId the identifier of the authority to group counts for
         * @return a list of Object[] where each array contains two elements: index 0 is the MissingPerson.MissingStatus, index 1 is a Long with the count for that status
         */
        @Query("SELECT m.status, COUNT(m) FROM MissingPerson m WHERE m.authorityId = :authorityId GROUP BY m.status")
        List<Object[]> countGroupByStatusByAuthority(Long authorityId);

        /**
                         * Searches MissingPerson records for a specific authority, optionally filtering by a free-text query
                         * (matches missingPersonName or caseNumber, case-insensitively) and/or by status, and returns results paged.
                         *
                         * @param authorityId the ID of the authority whose records to search
                         * @param search      optional text to match against missingPersonName or caseNumber; ignored if null or empty
                         * @param status      optional status to filter by; ignored if null
                         * @param pageable    pagination and sorting parameters
                         * @return            a page of MissingPerson entities that match the provided authority, text search, and status filters
                         */
                        @Query("SELECT m FROM MissingPerson m WHERE m.authorityId = :authorityId " +
                        "AND (:search IS NULL OR :search = '' OR LOWER(m.missingPersonName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.caseNumber) LIKE LOWER(CONCAT('%', :search, '%'))) "
                        +
                        "AND (:status IS NULL OR m.status = :status)")
        Page<MissingPerson> searchByAuthority(Long authorityId, String search, MissingPerson.MissingStatus status,
                        Pageable pageable);
}