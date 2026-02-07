package com.ocrs.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Update Entity Tests")
class UpdateTest {

    private Update update;

    @BeforeEach
    void setUp() {
        update = new Update();
    }

    @Test
    @DisplayName("Should create Update using builder pattern")
    void testBuilderPattern() {
        Update builtUpdate = Update.builder()
                .firId(1L)
                .missingPersonId(null)
                .authorityId(10L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("UNDER_INVESTIGATION")
                .comment("Investigation started by officer")
                .build();

        assertNotNull(builtUpdate);
        assertEquals(1L, builtUpdate.getFirId());
        assertNull(builtUpdate.getMissingPersonId());
        assertEquals(10L, builtUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.STATUS_CHANGE, builtUpdate.getUpdateType());
        assertEquals("PENDING", builtUpdate.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", builtUpdate.getNewStatus());
        assertEquals("Investigation started by officer", builtUpdate.getComment());
    }

    @Test
    @DisplayName("Should set timestamp on onCreate")
    void testOnCreate() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        update.onCreate();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(update.getCreatedAt());
        assertTrue(update.getCreatedAt().isAfter(before) && update.getCreatedAt().isBefore(after));
    }

    @Test
    @DisplayName("Should handle all UpdateType values")
    void testAllUpdateTypes() {
        Update.UpdateType[] types = Update.UpdateType.values();
        assertEquals(4, types.length);
        assertTrue(containsUpdateType(types, Update.UpdateType.STATUS_CHANGE));
        assertTrue(containsUpdateType(types, Update.UpdateType.COMMENT));
        assertTrue(containsUpdateType(types, Update.UpdateType.EVIDENCE_ADDED));
        assertTrue(containsUpdateType(types, Update.UpdateType.REASSIGNMENT));
    }

    @Test
    @DisplayName("Should allow null firId when missingPersonId is set")
    void testNullFirId() {
        update.setFirId(null);
        update.setMissingPersonId(5L);
        assertNull(update.getFirId());
        assertEquals(5L, update.getMissingPersonId());
    }

    @Test
    @DisplayName("Should allow null missingPersonId when firId is set")
    void testNullMissingPersonId() {
        update.setFirId(10L);
        update.setMissingPersonId(null);
        assertEquals(10L, update.getFirId());
        assertNull(update.getMissingPersonId());
    }

    @Test
    @DisplayName("Should allow null previousStatus")
    void testNullPreviousStatus() {
        update.setPreviousStatus(null);
        assertNull(update.getPreviousStatus());
    }

    @Test
    @DisplayName("Should allow null newStatus")
    void testNullNewStatus() {
        update.setNewStatus(null);
        assertNull(update.getNewStatus());
    }

    @Test
    @DisplayName("Should allow null comment")
    void testNullComment() {
        update.setComment(null);
        assertNull(update.getComment());
    }

    @Test
    @DisplayName("Should handle equality and hashCode")
    void testEqualsAndHashCode() {
        Update update1 = Update.builder()
                .id(1L)
                .firId(5L)
                .authorityId(10L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        Update update2 = Update.builder()
                .id(1L)
                .firId(5L)
                .authorityId(10L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        assertEquals(update1, update2);
        assertEquals(update1.hashCode(), update2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void testToString() {
        update.setUpdateType(Update.UpdateType.STATUS_CHANGE);
        update.setComment("Status updated");
        String toString = update.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("STATUS_CHANGE"));
        assertTrue(toString.contains("Status updated"));
    }

    @Test
    @DisplayName("Should handle no-args constructor")
    void testNoArgsConstructor() {
        Update emptyUpdate = new Update();
        assertNotNull(emptyUpdate);
        assertNull(emptyUpdate.getId());
        assertNull(emptyUpdate.getFirId());
        assertNull(emptyUpdate.getAuthorityId());
    }

    @Test
    @DisplayName("Should handle all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Update fullUpdate = new Update(
                1L,
                10L,
                20L,
                100L,
                Update.UpdateType.EVIDENCE_ADDED,
                "PENDING",
                "UNDER_INVESTIGATION",
                "New evidence submitted",
                now
        );

        assertNotNull(fullUpdate);
        assertEquals(1L, fullUpdate.getId());
        assertEquals(10L, fullUpdate.getFirId());
        assertEquals(20L, fullUpdate.getMissingPersonId());
        assertEquals(100L, fullUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.EVIDENCE_ADDED, fullUpdate.getUpdateType());
        assertEquals("PENDING", fullUpdate.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", fullUpdate.getNewStatus());
        assertEquals("New evidence submitted", fullUpdate.getComment());
        assertEquals(now, fullUpdate.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle STATUS_CHANGE update type")
    void testStatusChangeUpdate() {
        Update statusUpdate = Update.builder()
                .firId(1L)
                .authorityId(5L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("RESOLVED")
                .comment("Case resolved successfully")
                .build();

        assertEquals(Update.UpdateType.STATUS_CHANGE, statusUpdate.getUpdateType());
        assertEquals("PENDING", statusUpdate.getPreviousStatus());
        assertEquals("RESOLVED", statusUpdate.getNewStatus());
    }

    @Test
    @DisplayName("Should handle COMMENT update type")
    void testCommentUpdate() {
        Update commentUpdate = Update.builder()
                .firId(2L)
                .authorityId(7L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Additional information received from witness")
                .build();

        assertEquals(Update.UpdateType.COMMENT, commentUpdate.getUpdateType());
        assertNull(commentUpdate.getPreviousStatus());
        assertNull(commentUpdate.getNewStatus());
        assertNotNull(commentUpdate.getComment());
    }

    @Test
    @DisplayName("Should handle EVIDENCE_ADDED update type")
    void testEvidenceAddedUpdate() {
        Update evidenceUpdate = Update.builder()
                .firId(3L)
                .authorityId(8L)
                .updateType(Update.UpdateType.EVIDENCE_ADDED)
                .comment("Photos and video evidence added")
                .build();

        assertEquals(Update.UpdateType.EVIDENCE_ADDED, evidenceUpdate.getUpdateType());
        assertEquals("Photos and video evidence added", evidenceUpdate.getComment());
    }

    @Test
    @DisplayName("Should handle REASSIGNMENT update type")
    void testReassignmentUpdate() {
        Update reassignmentUpdate = Update.builder()
                .firId(4L)
                .authorityId(9L)
                .updateType(Update.UpdateType.REASSIGNMENT)
                .comment("Case reassigned to specialist team")
                .build();

        assertEquals(Update.UpdateType.REASSIGNMENT, reassignmentUpdate.getUpdateType());
        assertEquals("Case reassigned to specialist team", reassignmentUpdate.getComment());
    }

    @Test
    @DisplayName("Should handle long comment text")
    void testLongComment() {
        String longComment = "A".repeat(10000);
        update.setComment(longComment);
        assertEquals(longComment, update.getComment());
        assertEquals(10000, update.getComment().length());
    }

    @Test
    @DisplayName("Should preserve createdAt timestamp after onCreate")
    void testCreatedAtPreservation() throws InterruptedException {
        update.onCreate();
        LocalDateTime firstCreatedAt = update.getCreatedAt();

        Thread.sleep(10);
        update.onCreate(); // Call again

        // Should have a new timestamp since onCreate was called again
        assertNotNull(update.getCreatedAt());
        assertTrue(update.getCreatedAt().isAfter(firstCreatedAt) ||
                   update.getCreatedAt().isEqual(firstCreatedAt));
    }

    @Test
    @DisplayName("Should handle both firId and missingPersonId being null")
    void testBothIdsNull() {
        update.setFirId(null);
        update.setMissingPersonId(null);
        assertNull(update.getFirId());
        assertNull(update.getMissingPersonId());
    }

    @Test
    @DisplayName("Should handle both firId and missingPersonId being set")
    void testBothIdsSet() {
        update.setFirId(15L);
        update.setMissingPersonId(25L);
        assertEquals(15L, update.getFirId());
        assertEquals(25L, update.getMissingPersonId());
    }

    @Test
    @DisplayName("Should handle update for missing person case")
    void testMissingPersonUpdate() {
        Update missingPersonUpdate = Update.builder()
                .missingPersonId(100L)
                .authorityId(50L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("MISSING")
                .newStatus("FOUND")
                .comment("Person found safe")
                .build();

        assertNull(missingPersonUpdate.getFirId());
        assertEquals(100L, missingPersonUpdate.getMissingPersonId());
        assertEquals("FOUND", missingPersonUpdate.getNewStatus());
    }

    @Test
    @DisplayName("Should handle empty comment")
    void testEmptyComment() {
        update.setComment("");
        assertEquals("", update.getComment());
    }

    // Helper method
    private boolean containsUpdateType(Update.UpdateType[] types, Update.UpdateType type) {
        for (Update.UpdateType t : types) {
            if (t == type) return true;
        }
        return false;
    }
}