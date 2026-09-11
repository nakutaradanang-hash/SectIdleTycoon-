package com.sect.idle.apm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * ApmManagerTest - Comprehensive unit tests for Application Performance Monitoring.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ApmManagerTest {

    private ApmManager apmManager;

    @Before
    public void setUp() {
        apmManager = ApmManager.getInstance();
    }

    @Test
    public void testCpuMonitorSampling() {
        CpuMonitor cpuMonitor = apmManager.getCpuMonitor();
        Assert.assertNotNull(cpuMonitor);
        cpuMonitor.sample();
        float processCpu = cpuMonitor.getProcessCpuUsage();
        float systemCpu = cpuMonitor.getSystemCpuUsage();
        Assert.assertTrue("Process CPU should be non-negative", processCpu >= 0f);
        Assert.assertTrue("System CPU should be non-negative", systemCpu >= 0f);
        Assert.assertTrue("Processors count should be at least 1", cpuMonitor.getAvailableProcessors() >= 1);
    }

    @Test
    public void testMemoryWatchdogMetrics() {
        MemoryWatchdog memoryWatchdog = apmManager.getMemoryWatchdog();
        Assert.assertNotNull(memoryWatchdog);
        memoryWatchdog.sample();
        double usedHeap = memoryWatchdog.getUsedHeapMb();
        double maxHeap = memoryWatchdog.getMaxHeapMb();
        float percent = memoryWatchdog.getHeapUsagePercent();

        Assert.assertTrue("Max heap should be greater than 0", maxHeap > 0);
        Assert.assertTrue("Used heap should be >= 0", usedHeap >= 0);
        Assert.assertTrue("Heap percent should be between 0 and 100", percent >= 0.0f && percent <= 100.0f);
    }

    @Test
    public void testFrameLatencyTrackerMetrics() {
        FrameLatencyTracker tracker = apmManager.getFrameTracker();
        Assert.assertNotNull(tracker);
        
        // Simulate normal 60fps frames (16.6ms = 16_600_000ns)
        tracker.recordFrame(16600000L);
        tracker.recordFrame(16700000L);
        tracker.recordFrame(16500000L);

        Assert.assertTrue("FPS should be around 60", tracker.getFps() > 50f);
        Assert.assertTrue("Avg frame time should be positive", tracker.getAvgFrameMs() > 0f);

        // Simulate jank frame (120ms) and freeze (300ms)
        tracker.recordFrame(120000000L);
        tracker.recordFrame(300000000L);

        Assert.assertTrue("Jank count should be at least 1", tracker.getJankFrameCount() >= 1);
        Assert.assertTrue("Frozen count should be at least 1", tracker.getFrozenFrameCount() >= 1);
    }

    @Test
    public void testErrorRateTrackerMetrics() {
        ErrorRateTracker tracker = apmManager.getErrorTracker();
        Assert.assertNotNull(tracker);
        tracker.reset();

        NullPointerException npe = new NullPointerException("Test null pointer exception");
        OutOfMemoryError oom = new OutOfMemoryError("Test out of memory");
        IllegalStateException general = new IllegalStateException("Test runtime issue");

        tracker.recordException(npe, false);
        tracker.recordException(oom, false);
        tracker.recordException(general, true);

        Assert.assertEquals(3, tracker.getTotalExceptions());
        Assert.assertEquals(1, tracker.getNpeCount());
        Assert.assertEquals(1, tracker.getOomCount());
        Assert.assertEquals(1, tracker.getCrashCount());
        Assert.assertTrue("Errors per minute should be 3", tracker.getErrorsPerMinute() >= 3f);
    }

    @Test
    public void testLeakDetectorLifecycle() {
        LeakDetector leakDetector = apmManager.getLeakDetector();
        Assert.assertNotNull(leakDetector);
        leakDetector.clear();

        Object dummyObject = new Object();
        String key = leakDetector.watch(dummyObject, "DummyTestObject");
        Assert.assertNotNull(key);
        Assert.assertTrue(key.startsWith("leak_"));

        // Active leaks check immediately should be 0 because 5s threshold hasn't elapsed
        java.util.List<LeakDetector.LeakCandidate> initialLeaks = leakDetector.checkLeaks();
        Assert.assertEquals(0, initialLeaks.size());
    }

    @Test
    public void testAlertDispatchingAndListeners() {
        final ApmAlert[] alertHolder = new ApmAlert[1];
        ApmManager.ApmAlertListener listener = new ApmManager.ApmAlertListener() {
            @Override
            public void onAlertTriggered(ApmAlert alert) {
                alertHolder[0] = alert;
            }
        };

        apmManager.addAlertListener(listener);
        ApmAlert alert = new ApmAlert(
            ApmAlert.AlertType.HIGH_MEMORY,
            ApmAlert.Severity.WARNING,
            "High Memory Warning",
            "Heap exceeded 80%",
            "Trigger GC and clear cache",
            82.5,
            80.0
        );

        apmManager.dispatchAlert(alert);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        Assert.assertNotNull(alert.getFormattedTime());
        Assert.assertTrue(alert.getDisplaySummary().contains("High Memory Warning"));
        Assert.assertNotNull(alertHolder[0]);

        apmManager.removeAlertListener(listener);
    }

    @Test
    public void testJsonDiagnosticsExport() {
        String json = apmManager.exportJsonDiagnostics();
        Assert.assertNotNull(json);
        Assert.assertTrue("JSON should contain cpu metrics", json.contains("\"cpu\""));
        Assert.assertTrue("JSON should contain memory metrics", json.contains("\"memory\""));
        Assert.assertTrue("JSON should contain rendering metrics", json.contains("\"rendering\""));
        Assert.assertTrue("JSON should contain errors metrics", json.contains("\"errors\""));
    }
}
