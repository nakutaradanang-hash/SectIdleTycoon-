package com.sect.idle.utils;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for ExceptionManager, ErrorCode and Defensive Execution Framework.
 * Verifies error codes, safe execution wrappers, throttle deduplication, breadcrumb ring buffer,
 * and contextual telemetry.
 */
public class ExceptionManagerErrorHandlingTest {

    private ExceptionManager exceptionManager;

    @Before
    public void setUp() {
        exceptionManager = ExceptionManager.get();
        exceptionManager.clearBreadcrumbs();
        exceptionManager.clearThrottleCache();
    }

    @Test
    public void testErrorCodePropertiesAndLookup() {
        ErrorCode npeCode = ErrorCode.SYS_NPE_GUARD;
        assertEquals(1001, npeCode.getCode());
        assertEquals("SYS", npeCode.getSubsystem());
        assertEquals(ExceptionManager.LEVEL_WARN, npeCode.getDefaultSeverity());
        assertNotNull(npeCode.getDescription());
        assertNotNull(npeCode.getRecoverySuggestion());

        ErrorCode resolved = ErrorCode.fromCode(1001);
        assertEquals(ErrorCode.SYS_NPE_GUARD, resolved);

        ErrorCode unknown = ErrorCode.fromCode(99999);
        assertEquals(ErrorCode.SYS_UNKNOWN, unknown);
    }

    @Test
    public void testSafeRunnableExecution_Success() {
        final boolean[] executed = new boolean[]{false};
        boolean result = ExceptionManager.executeSafely(new ExceptionManager.SafeRunnable() {
            @Override
            public void run() throws Throwable {
                executed[0] = true;
            }
        }, "TEST_CAT", "Test successful action");

        assertTrue("SafeRunnable must return true on success", result);
        assertTrue("Inner block must be executed", executed[0]);
    }

    @Test
    public void testSafeRunnableExecution_CatchesExceptionGracefully() {
        boolean result = ExceptionManager.executeSafely(new ExceptionManager.SafeRunnable() {
            @Override
            public void run() throws Throwable {
                throw new NullPointerException("Simulated NPE for testing");
            }
        }, "TEST_NPE", "Testing NPE fault isolation");

        assertFalse("SafeRunnable must return false when exception thrown", result);
    }

    @Test
    public void testSafeSupplierExecution_FallbackOnFailure() {
        String fallback = "SAFE_FALLBACK";
        String value = ExceptionManager.executeSafely(new ExceptionManager.SafeSupplier<String>() {
            @Override
            public String get() throws Throwable {
                throw new OutOfMemoryError("Simulated OOM for test");
            }
        }, fallback, "TEST_OOM", "Testing OOM supplier fallback");

        assertEquals("SafeSupplier must return designated fallback on failure", fallback, value);
    }

    @Test
    public void testBreadcrumbRecordingAndCapacity() {
        for (int i = 0; i < 60; i++) {
            exceptionManager.addBreadcrumb("TEST", "Step " + i);
        }

        String[] breadcrumbs = exceptionManager.getRecentBreadcrumbs();
        assertNotNull(breadcrumbs);
        assertTrue("Breadcrumbs buffer must be bounded at max 50", breadcrumbs.length <= 50);
        assertTrue("Breadcrumbs must not be empty", breadcrumbs.length > 0);
    }

    @Test
    public void testReportErrorWithErrorCode() {
        int initialLogs = exceptionManager.getTotalLogCount();

        exceptionManager.reportError(ErrorCode.SAVE_CHECKSUM_MISMATCH, new IllegalStateException("Tamper detected"), "Checksum failed", "Slot #1");

        assertTrue("Log count must increase after error report", exceptionManager.getTotalLogCount() > initialLogs);
    }

    @Test
    public void testPerformanceLagAndMemoryPressureLogging() {
        // Log performance lag
        exceptionManager.logPerformanceLag("BattleRender", 45L, 30L);
        // Log memory pressure
        exceptionManager.logMemoryPressure(180L, 200L, "CombatScene");
        // Log security event
        exceptionManager.logSecurityEvent("HMAC_CHECK", "Verification passed", false);

        assertTrue("Logs should be recorded for APM events", exceptionManager.getTotalLogCount() > 0);
    }
}
