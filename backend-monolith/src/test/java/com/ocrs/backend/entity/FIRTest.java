package com.ocrs.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FIR Entity Tests")
class FIRTest {

    private FIR fir;

    @BeforeEach
    void setUp() {
        fir = new FIR();
    }

    @Test
    @DisplayName("Should create FIR with default values")
    void testDefaultValues() {
        assertEquals(FIR.Status.PENDING, fir.getStatus());
        assertEquals(FIR.Priority.MEDIUM, fir.getPriority());
    }

    @Test
    @DisplayName("Should create FIR using builder pattern")
    void testBuilderPattern() {
        FIR builtFir = FIR.builder()
                .firNumber("FIR-2024-001")
                .userId(1L)
                .authorityId(2L)
                .category(FIR.Category.THEFT)
                .title("Stolen Laptop")
                .description("My laptop was stolen from the office")
                .incidentDate(LocalDate.of(2024, 1, 15))
                .incidentTime(LocalTime.of(14, 30))
                .incidentLocation("123 Main St, Office Building")
                .status(FIR.Status.UNDER_INVESTIGATION)
                .priority(FIR.Priority.HIGH)
                .evidenceUrls("[\"url1\", \"url2\"]")
                .build();

        assertNotNull(builtFir);
        assertEquals("FIR-2024-001", builtFir.getFirNumber());
        assertEquals(1L, builtFir.getUserId());
        assertEquals(2L, builtFir.getAuthorityId());
        assertEquals(FIR.Category.THEFT, builtFir.getCategory());
        assertEquals("Stolen Laptop", builtFir.getTitle());
        assertEquals("My laptop was stolen from the office", builtFir.getDescription());
        assertEquals(LocalDate.of(2024, 1, 15), builtFir.getIncidentDate());
        assertEquals(LocalTime.of(14, 30), builtFir.getIncidentTime());
        assertEquals("123 Main St, Office Building", builtFir.getIncidentLocation());
        assertEquals(FIR.Status.UNDER_INVESTIGATION, builtFir.getStatus());
        assertEquals(FIR.Priority.HIGH, builtFir.getPriority());
        assertEquals("[\"url1\", \"url2\"]", builtFir.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should set timestamps on onCreate")
    void testOnCreate() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        fir.onCreate();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(fir.getCreatedAt());
        assertNotNull(fir.getUpdatedAt());
        assertTrue(fir.getCreatedAt().isAfter(before) && fir.getCreatedAt().isBefore(after));
        assertTrue(fir.getUpdatedAt().isAfter(before) && fir.getUpdatedAt().isBefore(after));
        assertEquals(fir.getCreatedAt(), fir.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update timestamp on onUpdate")
    void testOnUpdate() throws InterruptedException {
        fir.onCreate();
        LocalDateTime originalUpdatedAt = fir.getUpdatedAt();
        LocalDateTime originalCreatedAt = fir.getCreatedAt();

        Thread.sleep(10); // Ensure time difference

        fir.onUpdate();

        assertNotNull(fir.getUpdatedAt());
        assertTrue(fir.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(originalCreatedAt, fir.getCreatedAt()); // createdAt should not change
    }

    @Test
    @DisplayName("Should handle all FIR categories")
    void testAllCategories() {
        FIR.Category[] categories = FIR.Category.values();
        assertEquals(7, categories.length);
        assertTrue(containsCategory(categories, FIR.Category.THEFT));
        assertTrue(containsCategory(categories, FIR.Category.ASSAULT));
        assertTrue(containsCategory(categories, FIR.Category.FRAUD));
        assertTrue(containsCategory(categories, FIR.Category.CYBERCRIME));
        assertTrue(containsCategory(categories, FIR.Category.HARASSMENT));
        assertTrue(containsCategory(categories, FIR.Category.VANDALISM));
        assertTrue(containsCategory(categories, FIR.Category.OTHER));
    }

    @Test
    @DisplayName("Should handle all FIR statuses")
    void testAllStatuses() {
        FIR.Status[] statuses = FIR.Status.values();
        assertEquals(5, statuses.length);
        assertTrue(containsStatus(statuses, FIR.Status.PENDING));
        assertTrue(containsStatus(statuses, FIR.Status.UNDER_INVESTIGATION));
        assertTrue(containsStatus(statuses, FIR.Status.RESOLVED));
        assertTrue(containsStatus(statuses, FIR.Status.CLOSED));
        assertTrue(containsStatus(statuses, FIR.Status.REJECTED));
    }

    @Test
    @DisplayName("Should handle all FIR priorities")
    void testAllPriorities() {
        FIR.Priority[] priorities = FIR.Priority.values();
        assertEquals(4, priorities.length);
        assertTrue(containsPriority(priorities, FIR.Priority.LOW));
        assertTrue(containsPriority(priorities, FIR.Priority.MEDIUM));
        assertTrue(containsPriority(priorities, FIR.Priority.HIGH));
        assertTrue(containsPriority(priorities, FIR.Priority.URGENT));
    }

    @Test
    @DisplayName("Should allow null authorityId")
    void testNullAuthorityId() {
        fir.setAuthorityId(null);
        assertNull(fir.getAuthorityId());
    }

    @Test
    @DisplayName("Should allow null incidentTime")
    void testNullIncidentTime() {
        fir.setIncidentTime(null);
        assertNull(fir.getIncidentTime());
    }

    @Test
    @DisplayName("Should allow null evidenceUrls")
    void testNullEvidenceUrls() {
        fir.setEvidenceUrls(null);
        assertNull(fir.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should handle equality and hashCode")
    void testEqualsAndHashCode() {
        FIR fir1 = FIR.builder()
                .id(1L)
                .firNumber("FIR-001")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        FIR fir2 = FIR.builder()
                .id(1L)
                .firNumber("FIR-001")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        assertEquals(fir1, fir2);
        assertEquals(fir1.hashCode(), fir2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void testToString() {
        fir.setFirNumber("FIR-123");
        fir.setTitle("Test FIR");
        String toString = fir.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("FIR-123"));
        assertTrue(toString.contains("Test FIR"));
    }

    @Test
    @DisplayName("Should handle no-args constructor")
    void testNoArgsConstructor() {
        FIR emptyFir = new FIR();
        assertNotNull(emptyFir);
        assertNull(emptyFir.getId());
        assertNull(emptyFir.getFirNumber());
    }

    @Test
    @DisplayName("Should handle all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        FIR fullFir = new FIR(
                1L,
                "FIR-001",
                100L,
                200L,
                FIR.Category.ASSAULT,
                "Assault Case",
                "Detailed description",
                LocalDate.of(2024, 2, 1),
                LocalTime.of(10, 0),
                "Location details",
                FIR.Status.PENDING,
                FIR.Priority.HIGH,
                "[]",
                now,
                now
        );

        assertNotNull(fullFir);
        assertEquals(1L, fullFir.getId());
        assertEquals("FIR-001", fullFir.getFirNumber());
        assertEquals(100L, fullFir.getUserId());
        assertEquals(200L, fullFir.getAuthorityId());
        assertEquals(FIR.Category.ASSAULT, fullFir.getCategory());
    }

    @Test
    @DisplayName("Should preserve status through lifecycle")
    void testStatusPreservation() {
        fir.setStatus(FIR.Status.RESOLVED);
        fir.onCreate();
        assertEquals(FIR.Status.RESOLVED, fir.getStatus());

        fir.setStatus(FIR.Status.CLOSED);
        fir.onUpdate();
        assertEquals(FIR.Status.CLOSED, fir.getStatus());
    }

    @Test
    @DisplayName("Should handle edge case with empty evidence URLs")
    void testEmptyEvidenceUrls() {
        fir.setEvidenceUrls("");
        assertEquals("", fir.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should handle long description text")
    void testLongDescription() {
        String longDescription = "A".repeat(10000);
        fir.setDescription(longDescription);
        assertEquals(longDescription, fir.getDescription());
        assertEquals(10000, fir.getDescription().length());
    }

    // Helper methods
    private boolean containsCategory(FIR.Category[] categories, FIR.Category category) {
        for (FIR.Category c : categories) {
            if (c == category) return true;
        }
        return false;
    }

    private boolean containsStatus(FIR.Status[] statuses, FIR.Status status) {
        for (FIR.Status s : statuses) {
            if (s == status) return true;
        }
        return false;
    }

    private boolean containsPriority(FIR.Priority[] priorities, FIR.Priority priority) {
        for (FIR.Priority p : priorities) {
            if (p == priority) return true;
        }
        return false;
    }
}