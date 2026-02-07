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
    @DisplayName("Should create FIR with builder pattern")
    void testBuilderPattern() {
        // arrange & act
        LocalDate incidentDate = LocalDate.of(2024, 1, 15);
        LocalTime incidentTime = LocalTime.of(14, 30);

        FIR builtFir = FIR.builder()
                .firNumber("FIR-2024-001")
                .userId(1L)
                .authorityId(10L)
                .category(FIR.Category.THEFT)
                .title("Test FIR Title")
                .description("Detailed description of the incident")
                .incidentDate(incidentDate)
                .incidentTime(incidentTime)
                .incidentLocation("123 Main Street")
                .status(FIR.Status.PENDING)
                .priority(FIR.Priority.HIGH)
                .evidenceUrls("[\"url1\", \"url2\"]")
                .build();

        // assert
        assertNotNull(builtFir);
        assertEquals("FIR-2024-001", builtFir.getFirNumber());
        assertEquals(1L, builtFir.getUserId());
        assertEquals(10L, builtFir.getAuthorityId());
        assertEquals(FIR.Category.THEFT, builtFir.getCategory());
        assertEquals("Test FIR Title", builtFir.getTitle());
        assertEquals("Detailed description of the incident", builtFir.getDescription());
        assertEquals(incidentDate, builtFir.getIncidentDate());
        assertEquals(incidentTime, builtFir.getIncidentTime());
        assertEquals("123 Main Street", builtFir.getIncidentLocation());
        assertEquals(FIR.Status.PENDING, builtFir.getStatus());
        assertEquals(FIR.Priority.HIGH, builtFir.getPriority());
        assertEquals("[\"url1\", \"url2\"]", builtFir.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should have default status as PENDING")
    void testDefaultStatus() {
        // arrange & act
        FIR firWithDefaults = FIR.builder()
                .firNumber("FIR-2024-002")
                .userId(1L)
                .category(FIR.Category.ASSAULT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(FIR.Status.PENDING, firWithDefaults.getStatus());
    }

    @Test
    @DisplayName("Should have default priority as MEDIUM")
    void testDefaultPriority() {
        // arrange & act
        FIR firWithDefaults = FIR.builder()
                .firNumber("FIR-2024-003")
                .userId(1L)
                .category(FIR.Category.FRAUD)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(FIR.Priority.MEDIUM, firWithDefaults.getPriority());
    }

    @Test
    @DisplayName("Should set createdAt and updatedAt on persist")
    void testOnCreateCallback() {
        // arrange
        FIR newFir = FIR.builder()
                .firNumber("FIR-2024-004")
                .userId(1L)
                .category(FIR.Category.CYBERCRIME)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // act
        newFir.onCreate();

        // assert
        assertNotNull(newFir.getCreatedAt());
        assertNotNull(newFir.getUpdatedAt());
        assertEquals(newFir.getCreatedAt(), newFir.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update updatedAt on update")
    void testOnUpdateCallback() throws InterruptedException {
        // arrange
        FIR existingFir = FIR.builder()
                .firNumber("FIR-2024-005")
                .userId(1L)
                .category(FIR.Category.HARASSMENT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        existingFir.onCreate();
        LocalDateTime originalCreatedAt = existingFir.getCreatedAt();
        LocalDateTime originalUpdatedAt = existingFir.getUpdatedAt();

        // act
        Thread.sleep(10); // ensure time difference
        existingFir.onUpdate();

        // assert
        assertEquals(originalCreatedAt, existingFir.getCreatedAt());
        assertTrue(existingFir.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should support all Category enum values")
    void testAllCategoryValues() {
        // assert
        assertEquals(7, FIR.Category.values().length);
        assertNotNull(FIR.Category.valueOf("THEFT"));
        assertNotNull(FIR.Category.valueOf("ASSAULT"));
        assertNotNull(FIR.Category.valueOf("FRAUD"));
        assertNotNull(FIR.Category.valueOf("CYBERCRIME"));
        assertNotNull(FIR.Category.valueOf("HARASSMENT"));
        assertNotNull(FIR.Category.valueOf("VANDALISM"));
        assertNotNull(FIR.Category.valueOf("OTHER"));
    }

    @Test
    @DisplayName("Should support all Status enum values")
    void testAllStatusValues() {
        // assert
        assertEquals(5, FIR.Status.values().length);
        assertNotNull(FIR.Status.valueOf("PENDING"));
        assertNotNull(FIR.Status.valueOf("UNDER_INVESTIGATION"));
        assertNotNull(FIR.Status.valueOf("RESOLVED"));
        assertNotNull(FIR.Status.valueOf("CLOSED"));
        assertNotNull(FIR.Status.valueOf("REJECTED"));
    }

    @Test
    @DisplayName("Should support all Priority enum values")
    void testAllPriorityValues() {
        // assert
        assertEquals(4, FIR.Priority.values().length);
        assertNotNull(FIR.Priority.valueOf("LOW"));
        assertNotNull(FIR.Priority.valueOf("MEDIUM"));
        assertNotNull(FIR.Priority.valueOf("HIGH"));
        assertNotNull(FIR.Priority.valueOf("URGENT"));
    }

    @Test
    @DisplayName("Should allow null authorityId")
    void testNullableAuthorityId() {
        // arrange & act
        FIR firWithoutAuthority = FIR.builder()
                .firNumber("FIR-2024-006")
                .userId(1L)
                .category(FIR.Category.VANDALISM)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertNull(firWithoutAuthority.getAuthorityId());
    }

    @Test
    @DisplayName("Should allow null incidentTime")
    void testNullableIncidentTime() {
        // arrange & act
        FIR firWithoutTime = FIR.builder()
                .firNumber("FIR-2024-007")
                .userId(1L)
                .category(FIR.Category.OTHER)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertNull(firWithoutTime.getIncidentTime());
    }

    @Test
    @DisplayName("Should allow null evidenceUrls")
    void testNullableEvidenceUrls() {
        // arrange & act
        FIR firWithoutEvidence = FIR.builder()
                .firNumber("FIR-2024-008")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertNull(firWithoutEvidence.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should support status transition from PENDING to UNDER_INVESTIGATION")
    void testStatusTransition() {
        // arrange
        FIR firInProgress = FIR.builder()
                .firNumber("FIR-2024-009")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .status(FIR.Status.PENDING)
                .build();

        // act
        firInProgress.setStatus(FIR.Status.UNDER_INVESTIGATION);

        // assert
        assertEquals(FIR.Status.UNDER_INVESTIGATION, firInProgress.getStatus());
    }

    @Test
    @DisplayName("Should support priority escalation")
    void testPriorityEscalation() {
        // arrange
        FIR firToEscalate = FIR.builder()
                .firNumber("FIR-2024-010")
                .userId(1L)
                .category(FIR.Category.ASSAULT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .priority(FIR.Priority.LOW)
                .build();

        // act
        firToEscalate.setPriority(FIR.Priority.URGENT);

        // assert
        assertEquals(FIR.Priority.URGENT, firToEscalate.getPriority());
    }

    @Test
    @DisplayName("Should handle past incident dates")
    void testPastIncidentDate() {
        // arrange & act
        LocalDate pastDate = LocalDate.of(2023, 6, 15);
        FIR pastFir = FIR.builder()
                .firNumber("FIR-2024-011")
                .userId(1L)
                .category(FIR.Category.FRAUD)
                .title("Test")
                .description("Test description")
                .incidentDate(pastDate)
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(pastDate, pastFir.getIncidentDate());
        assertTrue(pastFir.getIncidentDate().isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("Should handle long description text")
    void testLongDescription() {
        // arrange
        String longDescription = "A".repeat(5000);

        // act
        FIR firWithLongDescription = FIR.builder()
                .firNumber("FIR-2024-012")
                .userId(1L)
                .category(FIR.Category.CYBERCRIME)
                .title("Test")
                .description(longDescription)
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(5000, firWithLongDescription.getDescription().length());
    }

    @Test
    @DisplayName("Should handle long location text")
    void testLongLocation() {
        // arrange
        String longLocation = "Building 123, Floor 45, Room 678, " + "Street details ".repeat(50);

        // act
        FIR firWithLongLocation = FIR.builder()
                .firNumber("FIR-2024-013")
                .userId(1L)
                .category(FIR.Category.HARASSMENT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation(longLocation)
                .build();

        // assert
        assertTrue(firWithLongLocation.getIncidentLocation().length() > 100);
    }

    @Test
    @DisplayName("Should test equals and hashCode with same data")
    void testEqualsAndHashCode() {
        // arrange
        FIR fir1 = FIR.builder()
                .id(1L)
                .firNumber("FIR-2024-014")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        FIR fir2 = FIR.builder()
                .id(1L)
                .firNumber("FIR-2024-014")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(fir1, fir2);
        assertEquals(fir1.hashCode(), fir2.hashCode());
    }

    @Test
    @DisplayName("Should test toString contains key fields")
    void testToString() {
        // arrange
        FIR firForString = FIR.builder()
                .firNumber("FIR-2024-015")
                .userId(1L)
                .category(FIR.Category.VANDALISM)
                .title("Test Title")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // act
        String firString = firForString.toString();

        // assert
        assertNotNull(firString);
        assertTrue(firString.contains("FIR-2024-015"));
        assertTrue(firString.contains("VANDALISM"));
    }

    @Test
    @DisplayName("Should handle multiple evidence URLs in JSON format")
    void testMultipleEvidenceUrls() {
        // arrange
        String jsonEvidence = "[\"https://example.com/photo1.jpg\", \"https://example.com/photo2.jpg\", \"https://example.com/video.mp4\"]";

        // act
        FIR firWithMultipleEvidence = FIR.builder()
                .firNumber("FIR-2024-016")
                .userId(1L)
                .category(FIR.Category.ASSAULT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .evidenceUrls(jsonEvidence)
                .build();

        // assert
        assertEquals(jsonEvidence, firWithMultipleEvidence.getEvidenceUrls());
        assertTrue(firWithMultipleEvidence.getEvidenceUrls().contains("photo1.jpg"));
        assertTrue(firWithMultipleEvidence.getEvidenceUrls().contains("video.mp4"));
    }

    @Test
    @DisplayName("Should allow updating all mutable fields")
    void testAllSetters() {
        // arrange
        FIR mutableFir = FIR.builder()
                .firNumber("FIR-2024-017")
                .userId(1L)
                .category(FIR.Category.THEFT)
                .title("Original Title")
                .description("Original description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Original location")
                .build();

        // act
        mutableFir.setAuthorityId(20L);
        mutableFir.setCategory(FIR.Category.FRAUD);
        mutableFir.setTitle("Updated Title");
        mutableFir.setDescription("Updated description");
        mutableFir.setIncidentTime(LocalTime.of(10, 30));
        mutableFir.setStatus(FIR.Status.RESOLVED);
        mutableFir.setPriority(FIR.Priority.HIGH);
        mutableFir.setEvidenceUrls("[\"new_evidence.pdf\"]");

        // assert
        assertEquals(20L, mutableFir.getAuthorityId());
        assertEquals(FIR.Category.FRAUD, mutableFir.getCategory());
        assertEquals("Updated Title", mutableFir.getTitle());
        assertEquals("Updated description", mutableFir.getDescription());
        assertEquals(LocalTime.of(10, 30), mutableFir.getIncidentTime());
        assertEquals(FIR.Status.RESOLVED, mutableFir.getStatus());
        assertEquals(FIR.Priority.HIGH, mutableFir.getPriority());
        assertEquals("[\"new_evidence.pdf\"]", mutableFir.getEvidenceUrls());
    }

    @Test
    @DisplayName("Should handle edge case of midnight incident time")
    void testMidnightIncidentTime() {
        // arrange & act
        FIR midnightFir = FIR.builder()
                .firNumber("FIR-2024-018")
                .userId(1L)
                .category(FIR.Category.OTHER)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentTime(LocalTime.MIDNIGHT)
                .incidentLocation("Test location")
                .build();

        // assert
        assertEquals(LocalTime.MIDNIGHT, midnightFir.getIncidentTime());
        assertEquals(0, midnightFir.getIncidentTime().getHour());
    }

    @Test
    @DisplayName("Should handle all possible status values in lifecycle")
    void testCompleteStatusLifecycle() {
        // arrange
        FIR lifecycleFir = FIR.builder()
                .firNumber("FIR-2024-019")
                .userId(1L)
                .category(FIR.Category.CYBERCRIME)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        // act & assert - transition through all statuses
        assertEquals(FIR.Status.PENDING, lifecycleFir.getStatus());

        lifecycleFir.setStatus(FIR.Status.UNDER_INVESTIGATION);
        assertEquals(FIR.Status.UNDER_INVESTIGATION, lifecycleFir.getStatus());

        lifecycleFir.setStatus(FIR.Status.RESOLVED);
        assertEquals(FIR.Status.RESOLVED, lifecycleFir.getStatus());

        lifecycleFir.setStatus(FIR.Status.CLOSED);
        assertEquals(FIR.Status.CLOSED, lifecycleFir.getStatus());
    }

    @Test
    @DisplayName("Should handle rejection status")
    void testRejectedStatus() {
        // arrange & act
        FIR rejectedFir = FIR.builder()
                .firNumber("FIR-2024-020")
                .userId(1L)
                .category(FIR.Category.OTHER)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .status(FIR.Status.REJECTED)
                .build();

        // assert
        assertEquals(FIR.Status.REJECTED, rejectedFir.getStatus());
    }
}