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
    @DisplayName("Should create Update with builder pattern")
    void testBuilderPattern() {
        // arrange & act
        Update builtUpdate = Update.builder()
                .firId(100L)
                .missingPersonId(null)
                .authorityId(5L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("UNDER_INVESTIGATION")
                .comment("Investigation has been initiated")
                .build();

        // assert
        assertNotNull(builtUpdate);
        assertEquals(100L, builtUpdate.getFirId());
        assertNull(builtUpdate.getMissingPersonId());
        assertEquals(5L, builtUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.STATUS_CHANGE, builtUpdate.getUpdateType());
        assertEquals("PENDING", builtUpdate.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", builtUpdate.getNewStatus());
        assertEquals("Investigation has been initiated", builtUpdate.getComment());
    }

    @Test
    @DisplayName("Should set createdAt on persist")
    void testOnCreateCallback() {
        // arrange
        Update newUpdate = Update.builder()
                .firId(101L)
                .authorityId(5L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        // act
        newUpdate.onCreate();

        // assert
        assertNotNull(newUpdate.getCreatedAt());
    }

    @Test
    @DisplayName("Should support all UpdateType enum values")
    void testAllUpdateTypeValues() {
        // assert
        assertEquals(4, Update.UpdateType.values().length);
        assertNotNull(Update.UpdateType.valueOf("STATUS_CHANGE"));
        assertNotNull(Update.UpdateType.valueOf("COMMENT"));
        assertNotNull(Update.UpdateType.valueOf("EVIDENCE_ADDED"));
        assertNotNull(Update.UpdateType.valueOf("REASSIGNMENT"));
    }

    @Test
    @DisplayName("Should create STATUS_CHANGE update")
    void testStatusChangeUpdate() {
        // arrange & act
        Update statusUpdate = Update.builder()
                .firId(102L)
                .authorityId(10L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("RESOLVED")
                .comment("Case resolved successfully")
                .build();

        // assert
        assertEquals(Update.UpdateType.STATUS_CHANGE, statusUpdate.getUpdateType());
        assertEquals("PENDING", statusUpdate.getPreviousStatus());
        assertEquals("RESOLVED", statusUpdate.getNewStatus());
    }

    @Test
    @DisplayName("Should create COMMENT update")
    void testCommentUpdate() {
        // arrange & act
        Update commentUpdate = Update.builder()
                .firId(103L)
                .authorityId(15L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Additional investigation required at the scene")
                .build();

        // assert
        assertEquals(Update.UpdateType.COMMENT, commentUpdate.getUpdateType());
        assertNotNull(commentUpdate.getComment());
        assertNull(commentUpdate.getPreviousStatus());
        assertNull(commentUpdate.getNewStatus());
    }

    @Test
    @DisplayName("Should create EVIDENCE_ADDED update")
    void testEvidenceAddedUpdate() {
        // arrange & act
        Update evidenceUpdate = Update.builder()
                .firId(104L)
                .authorityId(20L)
                .updateType(Update.UpdateType.EVIDENCE_ADDED)
                .comment("Security camera footage uploaded")
                .build();

        // assert
        assertEquals(Update.UpdateType.EVIDENCE_ADDED, evidenceUpdate.getUpdateType());
        assertEquals("Security camera footage uploaded", evidenceUpdate.getComment());
    }

    @Test
    @DisplayName("Should create REASSIGNMENT update")
    void testReassignmentUpdate() {
        // arrange & act
        Update reassignmentUpdate = Update.builder()
                .firId(105L)
                .authorityId(25L)
                .updateType(Update.UpdateType.REASSIGNMENT)
                .comment("Case reassigned to specialized cybercrime unit")
                .build();

        // assert
        assertEquals(Update.UpdateType.REASSIGNMENT, reassignmentUpdate.getUpdateType());
        assertEquals("Case reassigned to specialized cybercrime unit", reassignmentUpdate.getComment());
    }

    @Test
    @DisplayName("Should allow null firId for missing person updates")
    void testNullableFirId() {
        // arrange & act
        Update missingPersonUpdate = Update.builder()
                .missingPersonId(50L)
                .authorityId(30L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("ACTIVE")
                .newStatus("FOUND")
                .comment("Person found safe")
                .build();

        // assert
        assertNull(missingPersonUpdate.getFirId());
        assertEquals(50L, missingPersonUpdate.getMissingPersonId());
    }

    @Test
    @DisplayName("Should allow null missingPersonId for FIR updates")
    void testNullableMissingPersonId() {
        // arrange & act
        Update firUpdate = Update.builder()
                .firId(106L)
                .authorityId(35L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Regular FIR update")
                .build();

        // assert
        assertEquals(106L, firUpdate.getFirId());
        assertNull(firUpdate.getMissingPersonId());
    }

    @Test
    @DisplayName("Should allow null previousStatus")
    void testNullablePreviousStatus() {
        // arrange & act
        Update updateWithoutPrevious = Update.builder()
                .firId(107L)
                .authorityId(40L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Initial update")
                .build();

        // assert
        assertNull(updateWithoutPrevious.getPreviousStatus());
    }

    @Test
    @DisplayName("Should allow null newStatus")
    void testNullableNewStatus() {
        // arrange & act
        Update updateWithoutNew = Update.builder()
                .firId(108L)
                .authorityId(45L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Just a comment, no status change")
                .build();

        // assert
        assertNull(updateWithoutNew.getNewStatus());
    }

    @Test
    @DisplayName("Should allow null comment")
    void testNullableComment() {
        // arrange & act
        Update updateWithoutComment = Update.builder()
                .firId(109L)
                .authorityId(50L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("CLOSED")
                .build();

        // assert
        assertNull(updateWithoutComment.getComment());
    }

    @Test
    @DisplayName("Should handle long comment text")
    void testLongComment() {
        // arrange
        String longComment = "This is a very detailed update. " + "Details: ".repeat(200);

        // act
        Update updateWithLongComment = Update.builder()
                .firId(110L)
                .authorityId(55L)
                .updateType(Update.UpdateType.COMMENT)
                .comment(longComment)
                .build();

        // assert
        assertTrue(updateWithLongComment.getComment().length() > 1000);
    }

    @Test
    @DisplayName("Should test equals and hashCode with same data")
    void testEqualsAndHashCode() {
        // arrange
        Update update1 = Update.builder()
                .id(1L)
                .firId(111L)
                .authorityId(60L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        Update update2 = Update.builder()
                .id(1L)
                .firId(111L)
                .authorityId(60L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Test comment")
                .build();

        // assert
        assertEquals(update1, update2);
        assertEquals(update1.hashCode(), update2.hashCode());
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        // arrange
        Update updateForString = Update.builder()
                .firId(112L)
                .authorityId(65L)
                .updateType(Update.UpdateType.EVIDENCE_ADDED)
                .comment("New evidence")
                .build();

        // act
        String updateString = updateForString.toString();

        // assert
        assertNotNull(updateString);
        assertTrue(updateString.contains("EVIDENCE_ADDED"));
    }

    @Test
    @DisplayName("Should allow updating all mutable fields")
    void testAllSetters() {
        // arrange
        Update mutableUpdate = Update.builder()
                .firId(113L)
                .authorityId(70L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Original comment")
                .build();

        // act
        mutableUpdate.setFirId(200L);
        mutableUpdate.setMissingPersonId(300L);
        mutableUpdate.setAuthorityId(75L);
        mutableUpdate.setUpdateType(Update.UpdateType.STATUS_CHANGE);
        mutableUpdate.setPreviousStatus("OLD_STATUS");
        mutableUpdate.setNewStatus("NEW_STATUS");
        mutableUpdate.setComment("Updated comment");

        // assert
        assertEquals(200L, mutableUpdate.getFirId());
        assertEquals(300L, mutableUpdate.getMissingPersonId());
        assertEquals(75L, mutableUpdate.getAuthorityId());
        assertEquals(Update.UpdateType.STATUS_CHANGE, mutableUpdate.getUpdateType());
        assertEquals("OLD_STATUS", mutableUpdate.getPreviousStatus());
        assertEquals("NEW_STATUS", mutableUpdate.getNewStatus());
        assertEquals("Updated comment", mutableUpdate.getComment());
    }

    @Test
    @DisplayName("Should handle multiple status transitions")
    void testMultipleStatusTransitions() {
        // arrange
        Update transition1 = Update.builder()
                .firId(114L)
                .authorityId(80L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("PENDING")
                .newStatus("UNDER_INVESTIGATION")
                .comment("Started investigation")
                .build();

        Update transition2 = Update.builder()
                .firId(114L)
                .authorityId(80L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("UNDER_INVESTIGATION")
                .newStatus("RESOLVED")
                .comment("Investigation completed")
                .build();

        Update transition3 = Update.builder()
                .firId(114L)
                .authorityId(80L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("RESOLVED")
                .newStatus("CLOSED")
                .comment("Case closed")
                .build();

        // assert
        assertEquals("PENDING", transition1.getPreviousStatus());
        assertEquals("UNDER_INVESTIGATION", transition1.getNewStatus());
        assertEquals("UNDER_INVESTIGATION", transition2.getPreviousStatus());
        assertEquals("RESOLVED", transition2.getNewStatus());
        assertEquals("RESOLVED", transition3.getPreviousStatus());
        assertEquals("CLOSED", transition3.getNewStatus());
    }

    @Test
    @DisplayName("Should handle update with both FIR and missing person IDs")
    void testBothIdsPresent() {
        // arrange & act
        Update crossReferenceUpdate = Update.builder()
                .firId(115L)
                .missingPersonId(60L)
                .authorityId(85L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("This FIR is related to a missing person case")
                .build();

        // assert
        assertEquals(115L, crossReferenceUpdate.getFirId());
        assertEquals(60L, crossReferenceUpdate.getMissingPersonId());
    }

    @Test
    @DisplayName("Should handle special characters in comment")
    void testSpecialCharactersInComment() {
        // arrange
        String specialComment = "Update: Case #123 - Evidence found @ location. Cost: $500 (approx). Contact: officer@police.gov";

        // act
        Update updateWithSpecialChars = Update.builder()
                .firId(116L)
                .authorityId(90L)
                .updateType(Update.UpdateType.COMMENT)
                .comment(specialComment)
                .build();

        // assert
        assertEquals(specialComment, updateWithSpecialChars.getComment());
        assertTrue(updateWithSpecialChars.getComment().contains("#123"));
        assertTrue(updateWithSpecialChars.getComment().contains("@"));
        assertTrue(updateWithSpecialChars.getComment().contains("$500"));
    }

    @Test
    @DisplayName("Should handle update with empty status strings")
    void testEmptyStatusStrings() {
        // arrange & act
        Update updateWithEmptyStatus = Update.builder()
                .firId(117L)
                .authorityId(95L)
                .updateType(Update.UpdateType.COMMENT)
                .previousStatus("")
                .newStatus("")
                .comment("Test empty status")
                .build();

        // assert
        assertEquals("", updateWithEmptyStatus.getPreviousStatus());
        assertEquals("", updateWithEmptyStatus.getNewStatus());
    }

    @Test
    @DisplayName("Should track creation time accurately")
    void testCreationTimeAccuracy() {
        // arrange
        LocalDateTime beforeCreate = LocalDateTime.now();
        Update timeTrackedUpdate = Update.builder()
                .firId(118L)
                .authorityId(100L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Time tracking test")
                .build();

        // act
        timeTrackedUpdate.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();

        // assert
        assertNotNull(timeTrackedUpdate.getCreatedAt());
        assertFalse(timeTrackedUpdate.getCreatedAt().isBefore(beforeCreate));
        assertFalse(timeTrackedUpdate.getCreatedAt().isAfter(afterCreate));
    }

    @Test
    @DisplayName("Should handle rejection to pending status reversal")
    void testStatusReversal() {
        // arrange & act
        Update reversalUpdate = Update.builder()
                .firId(119L)
                .authorityId(105L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("REJECTED")
                .newStatus("PENDING")
                .comment("Case reopened after review")
                .build();

        // assert
        assertEquals("REJECTED", reversalUpdate.getPreviousStatus());
        assertEquals("PENDING", reversalUpdate.getNewStatus());
    }

    @Test
    @DisplayName("Should handle multiline comment text")
    void testMultilineComment() {
        // arrange
        String multilineComment = "Line 1: Initial investigation\nLine 2: Evidence collected\nLine 3: Witness statements recorded";

        // act
        Update multilineUpdate = Update.builder()
                .firId(120L)
                .authorityId(110L)
                .updateType(Update.UpdateType.COMMENT)
                .comment(multilineComment)
                .build();

        // assert
        assertEquals(multilineComment, multilineUpdate.getComment());
        assertTrue(multilineUpdate.getComment().contains("\n"));
    }

    @Test
    @DisplayName("Should support evidence documentation workflow")
    void testEvidenceWorkflow() {
        // arrange & act
        Update evidenceReceived = Update.builder()
                .firId(121L)
                .authorityId(115L)
                .updateType(Update.UpdateType.EVIDENCE_ADDED)
                .comment("Physical evidence received and logged")
                .build();

        Update evidenceAnalyzed = Update.builder()
                .firId(121L)
                .authorityId(115L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Forensic analysis completed")
                .build();

        Update evidenceProcessed = Update.builder()
                .firId(121L)
                .authorityId(115L)
                .updateType(Update.UpdateType.STATUS_CHANGE)
                .previousStatus("UNDER_INVESTIGATION")
                .newStatus("RESOLVED")
                .comment("Evidence confirmed suspect's involvement")
                .build();

        // assert
        assertEquals(Update.UpdateType.EVIDENCE_ADDED, evidenceReceived.getUpdateType());
        assertEquals(Update.UpdateType.COMMENT, evidenceAnalyzed.getUpdateType());
        assertEquals(Update.UpdateType.STATUS_CHANGE, evidenceProcessed.getUpdateType());
        assertEquals("RESOLVED", evidenceProcessed.getNewStatus());
    }

    @Test
    @DisplayName("Should handle case reassignment between authorities")
    void testCaseReassignment() {
        // arrange & act
        Update originalAssignment = Update.builder()
                .firId(122L)
                .authorityId(120L)
                .updateType(Update.UpdateType.COMMENT)
                .comment("Case initially assigned to local precinct")
                .build();

        Update reassignment = Update.builder()
                .firId(122L)
                .authorityId(125L)
                .updateType(Update.UpdateType.REASSIGNMENT)
                .comment("Case transferred to state criminal investigation department")
                .build();

        // assert
        assertEquals(120L, originalAssignment.getAuthorityId());
        assertEquals(125L, reassignment.getAuthorityId());
        assertEquals(Update.UpdateType.REASSIGNMENT, reassignment.getUpdateType());
    }
}