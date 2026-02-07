package com.ocrs.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTest {

    private Update update;

    @BeforeEach
    void setUp() {
        update = new Update();
    }

    @Test
    void testNoArgsConstructor() {
        Update newUpdate = new Update();
        assertNotNull(newUpdate);
        assertNull(newUpdate.getId());
        assertNull(newUpdate.getFirId());
        assertNull(newUpdate.getMissingPersonId());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();

        Update newUpdate = new Update(
                1L,
                100L,
                null,
                200L,
                Update.UpdateType.STATUS_CHANGE,
                "PENDING",
                "UNDER_INVESTIGATION",
                "Case is now under active investigation",
                now
        );

        assertEquals(1L, newUpdate.getId());
        assertEquals(100L, newUpdate.getFirId());
        assertNull(newUpdate.getMissingPersonId());
        assertEquals(200L, newUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.STATUS_CHANGE, newUpdate.getUpdateType());
        assertEquals("PENDING", newUpdate.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", newUpdate.getNewStatus());
        assertEquals("Case is now under active investigation", newUpdate.getComment());
        assertEquals(now, newUpdate.getCreatedAt());
    }

    @Test
    void testBuilderPattern() {
        Update builtUpdate = Update.builder()
                .id(5L)
                .firId(500L)
                .authorityId(600L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Additional evidence has been collected")
                .build();

        assertEquals(5L, builtUpdate.getId());
        assertEquals(500L, builtUpdate.getFirId());
        assertNull(builtUpdate.getMissingPersonId());
        assertEquals(600L, builtUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.COMMENT, builtUpdate.getUpdateType());
        assertNull(builtUpdate.getPreviousStatus());
        assertNull(builtUpdate.getNewStatus());
        assertEquals("Additional evidence has been collected", builtUpdate.getComment());
    }

    @Test
    void testBuilderForMissingPerson() {
        Update builtUpdate = Update.builder()
                .id(10L)
                .missingPersonId(1000L)
                .authorityId(700L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("ACTIVE")
                .newStatus("FOUND")
                .comment("Missing person has been located")
                .build();

        assertEquals(10L, builtUpdate.getId());
        assertNull(builtUpdate.getFirId());
        assertEquals(1000L, builtUpdate.getMissingPersonId());
        assertEquals(700L, builtUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.STATUS_CHANGE, builtUpdate.getUpdateType());
        assertEquals("ACTIVE", builtUpdate.getPreviousStatus());
        assertEquals("FOUND", builtUpdate.getNewStatus());
        assertEquals("Missing person has been located", builtUpdate.getComment());
    }

    @Test
    void testOnCreateSetsTimestamp() {
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        update.onCreate();

        assertNotNull(update.getCreatedAt());
        assertTrue(update.getCreatedAt().isAfter(beforeCreate) || update.getCreatedAt().isEqual(beforeCreate));
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();

        update.setId(20L);
        update.setFirId(2000L);
        update.setMissingPersonId(3000L);
        update.setAuthorityId(4000L);
        update.setUpdateType(Update.UpdateType.EVIDENCE_ADDED);
        update.setPreviousStatus("OLD_STATUS");
        update.setNewStatus("NEW_STATUS");
        update.setComment("Evidence photos uploaded");
        update.setCreatedAt(timestamp);

        assertEquals(20L, update.getId());
        assertEquals(2000L, update.getFirId());
        assertEquals(3000L, update.getMissingPersonId());
        assertEquals(4000L, update.getAuthorityId());
        assertEquals(Update.UpdateType.EVIDENCE_ADDED, update.getUpdateType());
        assertEquals("OLD_STATUS", update.getPreviousStatus());
        assertEquals("NEW_STATUS", update.getNewStatus());
        assertEquals("Evidence photos uploaded", update.getComment());
        assertEquals(timestamp, update.getCreatedAt());
    }

    @Test
    void testUpdateTypeEnum() {
        assertEquals(4, Update.UpdateType.values().length);
        assertNotNull(Update.UpdateType.valueOf("STATUS_CHANGE"));
        assertNotNull(Update.UpdateType.valueOf("COMMENT"));
        assertNotNull(Update.UpdateType.valueOf("EVIDENCE_ADDED"));
        assertNotNull(Update.UpdateType.valueOf("REASSIGNMENT"));
    }

    @Test
    void testEqualsAndHashCode() {
        Update update1 = Update.builder()
                .id(1L)
                .firId(100L)
                .authorityId(200L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        Update update2 = Update.builder()
                .id(1L)
                .firId(100L)
                .authorityId(200L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        Update update3 = Update.builder()
                .id(2L)
                .firId(300L)
                .authorityId(400L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .comment("Different comment")
                .build();

        assertEquals(update1, update2);
        assertNotEquals(update1, update3);
        assertEquals(update1.hashCode(), update2.hashCode());
    }

    @Test
    void testToString() {
        Update testUpdate = Update.builder()
                .id(1L)
                .firId(100L)
                .authorityId(200L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("RESOLVED")
                .comment("Case resolved")
                .build();

        String toString = testUpdate.toString();
        assertTrue(toString.contains("Update"));
        assertTrue(toString.contains("STATUS_CHANGE"));
    }

    @Test
    void testStatusChangeUpdate() {
        Update statusUpdate = Update.builder()
                .firId(123L)
                .authorityId(456L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("UNDER_INVESTIGATION")
                .comment("Investigation started by Officer Smith")
                .build();

        assertEquals(Update.UpdateType.STATUS_CHANGE, statusUpdate.getUpdateType());
        assertEquals("PENDING", statusUpdate.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", statusUpdate.getNewStatus());
        assertNotNull(statusUpdate.getComment());
    }

    @Test
    void testCommentOnlyUpdate() {
        Update commentUpdate = Update.builder()
                .firId(789L)
                .authorityId(321L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Witness statement recorded")
                .build();

        assertEquals(Update.UpdateType.COMMENT, commentUpdate.getUpdateType());
        assertNull(commentUpdate.getPreviousStatus());
        assertNull(commentUpdate.getNewStatus());
        assertEquals("Witness statement recorded", commentUpdate.getComment());
    }

    @Test
    void testEvidenceAddedUpdate() {
        Update evidenceUpdate = Update.builder()
                .firId(555L)
                .authorityId(666L)
                .updateType(Update.UpdateType.EVIDENCE_ADDED)
                .comment("CCTV footage uploaded")
                .build();

        assertEquals(Update.UpdateType.EVIDENCE_ADDED, evidenceUpdate.getUpdateType());
        assertEquals("CCTV footage uploaded", evidenceUpdate.getComment());
    }

    @Test
    void testReassignmentUpdate() {
        Update reassignmentUpdate = Update.builder()
                .firId(999L)
                .authorityId(111L)
                .updateType(Update.UpdateType.REASSIGNMENT)
                .previousStatus("PENDING")
                .newStatus("PENDING")
                .comment("Case reassigned to Detective Jones")
                .build();

        assertEquals(Update.UpdateType.REASSIGNMENT, reassignmentUpdate.getUpdateType());
        assertEquals("Case reassigned to Detective Jones", reassignmentUpdate.getComment());
    }

    @Test
    void testNullableFields() {
        Update testUpdate = Update.builder()
                .authorityId(100L)
                .updateType(Update.UpdateType.COMMENT)
                .build();

        assertNull(testUpdate.getId());
        assertNull(testUpdate.getFirId());
        assertNull(testUpdate.getMissingPersonId());
        assertNull(testUpdate.getPreviousStatus());
        assertNull(testUpdate.getNewStatus());
        assertNull(testUpdate.getComment());
        assertNull(testUpdate.getCreatedAt());
    }

    @Test
    void testMutuallyExclusiveFirAndMissingPerson() {
        Update firUpdate = Update.builder()
                .firId(100L)
                .authorityId(200L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("FIR update")
                .build();

        Update missingPersonUpdate = Update.builder()
                .missingPersonId(300L)
                .authorityId(400L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Missing person update")
                .build();

        assertNotNull(firUpdate.getFirId());
        assertNull(firUpdate.getMissingPersonId());
        assertNull(missingPersonUpdate.getFirId());
        assertNotNull(missingPersonUpdate.getMissingPersonId());
    }

    @Test
    void testOnCreatePreservesExistingTimestamp() {
        LocalDateTime existingTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0);
        update.setCreatedAt(existingTimestamp);

        update.onCreate();

        assertNotEquals(existingTimestamp, update.getCreatedAt());
        assertTrue(update.getCreatedAt().isAfter(existingTimestamp));
    }

    @Test
    void testMultipleOnCreateCalls() throws InterruptedException {
        update.onCreate();
        LocalDateTime firstTimestamp = update.getCreatedAt();

        Thread.sleep(10);
        update.onCreate();
        LocalDateTime secondTimestamp = update.getCreatedAt();

        assertTrue(secondTimestamp.isAfter(firstTimestamp));
    }
}