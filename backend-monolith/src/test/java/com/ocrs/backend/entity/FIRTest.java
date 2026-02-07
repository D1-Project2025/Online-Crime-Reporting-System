package com.ocrs.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FIRTest {

    private FIR fir;

    @BeforeEach
    void setUp() {
        fir = new FIR();
    }

    @Test
    void testNoArgsConstructor() {
        FIR newFir = new FIR();
        assertNotNull(newFir);
        assertNull(newFir.getId());
        assertNull(newFir.getFirNumber());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDate incidentDate = LocalDate.of(2024, 1, 15);
        LocalTime incidentTime = LocalTime.of(14, 30);
        LocalDateTime now = LocalDateTime.now();

        FIR newFir = new FIR(
                1L,
                "FIR123456",
                100L,
                200L,
                FIR.Category.THEFT,
                "Stolen Vehicle",
                "Vehicle stolen from parking lot",
                incidentDate,
                incidentTime,
                "123 Main St",
                FIR.Status.PENDING,
                FIR.Priority.HIGH,
                "[\"evidence1.jpg\"]",
                now,
                now
        );

        assertEquals(1L, newFir.getId());
        assertEquals("FIR123456", newFir.getFirNumber());
        assertEquals(100L, newFir.getUserId());
        assertEquals(200L, newFir.getAuthorityId());
        assertEquals(FIR.Category.THEFT, newFir.getCategory());
        assertEquals("Stolen Vehicle", newFir.getTitle());
        assertEquals("Vehicle stolen from parking lot", newFir.getDescription());
        assertEquals(incidentDate, newFir.getIncidentDate());
        assertEquals(incidentTime, newFir.getIncidentTime());
        assertEquals("123 Main St", newFir.getIncidentLocation());
        assertEquals(FIR.Status.PENDING, newFir.getStatus());
        assertEquals(FIR.Priority.HIGH, newFir.getPriority());
        assertEquals("[\"evidence1.jpg\"]", newFir.getEvidenceUrls());
        assertEquals(now, newFir.getCreatedAt());
        assertEquals(now, newFir.getUpdatedAt());
    }

    @Test
    void testBuilderPattern() {
        LocalDate incidentDate = LocalDate.of(2024, 2, 20);
        LocalTime incidentTime = LocalTime.of(10, 15);

        FIR builtFir = FIR.builder()
                .id(5L)
                .firNumber("FIR789012")
                .userId(300L)
                .authorityId(400L)
                .category(FIR.Category.ASSAULT)
                .title("Physical Assault")
                .description("Assault incident description")
                .incidentDate(incidentDate)
                .incidentTime(incidentTime)
                .incidentLocation("456 Oak Ave")
                .status(FIR.Status.UNDER_INVESTIGATION)
                .priority(FIR.Priority.URGENT)
                .evidenceUrls("[\"photo1.jpg\", \"video1.mp4\"]")
                .build();

        assertEquals(5L, builtFir.getId());
        assertEquals("FIR789012", builtFir.getFirNumber());
        assertEquals(300L, builtFir.getUserId());
        assertEquals(400L, builtFir.getAuthorityId());
        assertEquals(FIR.Category.ASSAULT, builtFir.getCategory());
        assertEquals("Physical Assault", builtFir.getTitle());
        assertEquals("Assault incident description", builtFir.getDescription());
        assertEquals(incidentDate, builtFir.getIncidentDate());
        assertEquals(incidentTime, builtFir.getIncidentTime());
        assertEquals("456 Oak Ave", builtFir.getIncidentLocation());
        assertEquals(FIR.Status.UNDER_INVESTIGATION, builtFir.getStatus());
        assertEquals(FIR.Priority.URGENT, builtFir.getPriority());
        assertEquals("[\"photo1.jpg\", \"video1.mp4\"]", builtFir.getEvidenceUrls());
    }

    @Test
    void testBuilderWithDefaultValues() {
        FIR builtFir = FIR.builder()
                .firNumber("FIR111111")
                .userId(500L)
                .category(FIR.Category.CYBERCRIME)
                .title("Phishing Attack")
                .description("Received phishing email")
                .incidentDate(LocalDate.now())
                .incidentLocation("Online")
                .build();

        assertEquals(FIR.Status.PENDING, builtFir.getStatus());
        assertEquals(FIR.Priority.MEDIUM, builtFir.getPriority());
    }

    @Test
    void testOnCreateSetsTimestamps() {
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        fir.onCreate();

        assertNotNull(fir.getCreatedAt());
        assertNotNull(fir.getUpdatedAt());
        assertTrue(fir.getCreatedAt().isAfter(beforeCreate) || fir.getCreatedAt().isEqual(beforeCreate));
        assertTrue(fir.getUpdatedAt().isAfter(beforeCreate) || fir.getUpdatedAt().isEqual(beforeCreate));
        assertEquals(fir.getCreatedAt(), fir.getUpdatedAt());
    }

    @Test
    void testOnUpdateUpdatesTimestamp() throws InterruptedException {
        fir.onCreate();
        LocalDateTime initialUpdatedAt = fir.getUpdatedAt();

        Thread.sleep(10);
        fir.onUpdate();

        assertNotNull(fir.getUpdatedAt());
        assertTrue(fir.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void testSettersAndGetters() {
        fir.setId(10L);
        fir.setFirNumber("FIR999999");
        fir.setUserId(600L);
        fir.setAuthorityId(700L);
        fir.setCategory(FIR.Category.FRAUD);
        fir.setTitle("Credit Card Fraud");
        fir.setDescription("Unauthorized transactions detected");
        fir.setIncidentDate(LocalDate.of(2024, 3, 10));
        fir.setIncidentTime(LocalTime.of(16, 45));
        fir.setIncidentLocation("Online Banking");
        fir.setStatus(FIR.Status.RESOLVED);
        fir.setPriority(FIR.Priority.LOW);
        fir.setEvidenceUrls("[\"statement.pdf\"]");

        assertEquals(10L, fir.getId());
        assertEquals("FIR999999", fir.getFirNumber());
        assertEquals(600L, fir.getUserId());
        assertEquals(700L, fir.getAuthorityId());
        assertEquals(FIR.Category.FRAUD, fir.getCategory());
        assertEquals("Credit Card Fraud", fir.getTitle());
        assertEquals("Unauthorized transactions detected", fir.getDescription());
        assertEquals(LocalDate.of(2024, 3, 10), fir.getIncidentDate());
        assertEquals(LocalTime.of(16, 45), fir.getIncidentTime());
        assertEquals("Online Banking", fir.getIncidentLocation());
        assertEquals(FIR.Status.RESOLVED, fir.getStatus());
        assertEquals(FIR.Priority.LOW, fir.getPriority());
        assertEquals("[\"statement.pdf\"]", fir.getEvidenceUrls());
    }

    @Test
    void testCategoryEnum() {
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
    void testStatusEnum() {
        assertEquals(5, FIR.Status.values().length);
        assertNotNull(FIR.Status.valueOf("PENDING"));
        assertNotNull(FIR.Status.valueOf("UNDER_INVESTIGATION"));
        assertNotNull(FIR.Status.valueOf("RESOLVED"));
        assertNotNull(FIR.Status.valueOf("CLOSED"));
        assertNotNull(FIR.Status.valueOf("REJECTED"));
    }

    @Test
    void testPriorityEnum() {
        assertEquals(4, FIR.Priority.values().length);
        assertNotNull(FIR.Priority.valueOf("LOW"));
        assertNotNull(FIR.Priority.valueOf("MEDIUM"));
        assertNotNull(FIR.Priority.valueOf("HIGH"));
        assertNotNull(FIR.Priority.valueOf("URGENT"));
    }

    @Test
    void testEqualsAndHashCode() {
        FIR fir1 = FIR.builder()
                .id(1L)
                .firNumber("FIR001")
                .userId(100L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        FIR fir2 = FIR.builder()
                .id(1L)
                .firNumber("FIR001")
                .userId(100L)
                .category(FIR.Category.THEFT)
                .title("Test")
                .description("Test description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Test location")
                .build();

        FIR fir3 = FIR.builder()
                .id(2L)
                .firNumber("FIR002")
                .userId(200L)
                .category(FIR.Category.ASSAULT)
                .title("Different")
                .description("Different description")
                .incidentDate(LocalDate.now())
                .incidentLocation("Different location")
                .build();

        assertEquals(fir1, fir2);
        assertNotEquals(fir1, fir3);
        assertEquals(fir1.hashCode(), fir2.hashCode());
    }

    @Test
    void testToString() {
        FIR testFir = FIR.builder()
                .id(1L)
                .firNumber("FIR123")
                .userId(100L)
                .category(FIR.Category.THEFT)
                .title("Test Title")
                .description("Test Description")
                .incidentDate(LocalDate.of(2024, 1, 1))
                .incidentLocation("Test Location")
                .build();

        String toString = testFir.toString();
        assertTrue(toString.contains("FIR"));
        assertTrue(toString.contains("FIR123"));
        assertTrue(toString.contains("Test Title"));
    }

    @Test
    void testNullableFields() {
        FIR testFir = FIR.builder()
                .firNumber("FIR000")
                .userId(100L)
                .category(FIR.Category.OTHER)
                .title("Test")
                .description("Test")
                .incidentDate(LocalDate.now())
                .incidentLocation("Location")
                .build();

        assertNull(testFir.getId());
        assertNull(testFir.getAuthorityId());
        assertNull(testFir.getIncidentTime());
        assertNull(testFir.getEvidenceUrls());
        assertNull(testFir.getCreatedAt());
        assertNull(testFir.getUpdatedAt());
    }

    @Test
    void testMultipleUpdates() throws InterruptedException {
        fir.onCreate();
        LocalDateTime createdAt = fir.getCreatedAt();
        LocalDateTime firstUpdate = fir.getUpdatedAt();

        Thread.sleep(10);
        fir.onUpdate();
        LocalDateTime secondUpdate = fir.getUpdatedAt();

        Thread.sleep(10);
        fir.onUpdate();
        LocalDateTime thirdUpdate = fir.getUpdatedAt();

        assertEquals(createdAt, fir.getCreatedAt());
        assertTrue(secondUpdate.isAfter(firstUpdate));
        assertTrue(thirdUpdate.isAfter(secondUpdate));
    }

    @Test
    void testStatusTransitions() {
        fir.setStatus(FIR.Status.PENDING);
        assertEquals(FIR.Status.PENDING, fir.getStatus());

        fir.setStatus(FIR.Status.UNDER_INVESTIGATION);
        assertEquals(FIR.Status.UNDER_INVESTIGATION, fir.getStatus());

        fir.setStatus(FIR.Status.RESOLVED);
        assertEquals(FIR.Status.RESOLVED, fir.getStatus());

        fir.setStatus(FIR.Status.CLOSED);
        assertEquals(FIR.Status.CLOSED, fir.getStatus());
    }

    @Test
    void testPriorityChanges() {
        fir.setPriority(FIR.Priority.LOW);
        assertEquals(FIR.Priority.LOW, fir.getPriority());

        fir.setPriority(FIR.Priority.HIGH);
        assertEquals(FIR.Priority.HIGH, fir.getPriority());

        fir.setPriority(FIR.Priority.URGENT);
        assertEquals(FIR.Priority.URGENT, fir.getPriority());
    }
}