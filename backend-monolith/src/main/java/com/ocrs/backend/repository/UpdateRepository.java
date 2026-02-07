package com.ocrs.backend.repository;

import com.ocrs.backend.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpdateRepository extends JpaRepository<Update, Long> {

        /**
 * Retrieve updates associated with a specific FIR, ordered from newest to oldest.
 *
 * @param firId the identifier of the FIR to filter updates by
 * @return a list of Update entities matching the specified FIR id ordered by `createdAt` descending; an empty list if none are found
 */
List<Update> findByFirIdOrderByCreatedAtDesc(Long firId);

        /**
 * Retrieves updates for a specific missing person, ordered by creation time descending.
 *
 * @param missingPersonId the ID of the missing person whose updates to retrieve
 * @return a list of Update entities matching the given missingPersonId, ordered by `createdAt` descending
 */
List<Update> findByMissingPersonIdOrderByCreatedAtDesc(Long missingPersonId);

        /**
 * Retrieves updates associated with a specific authority, ordered from newest to oldest.
 *
 * @param authorityId the ID of the authority whose updates should be returned
 * @return a list of Update entities for the given authority ordered by `createdAt` descending
 */
List<Update> findByAuthorityIdOrderByCreatedAtDesc(Long authorityId);
}