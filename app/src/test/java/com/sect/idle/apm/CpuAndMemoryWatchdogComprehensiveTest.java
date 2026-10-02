package com.sect.idle.apm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CpuAndMemoryWatchdogComprehensiveTest {

    private CpuMonitor cpuMonitor;
    private MemoryWatchdog memoryWatchdog;
    private FrameLatencyTracker frameTracker;

    @Before
    public void setUp() {
        cpuMonitor = new CpuMonitor();
        memoryWatchdog = new MemoryWatchdog();
        frameTracker = new FrameLatencyTracker();
    }

    @Test
    public void testCpuMonitorSampling() {
        cpuMonitor.sample();
        Assert.assertTrue("CPU usage between 0 and 100", cpuMonitor.getProcessCpuUsage() >= 0f && cpuMonitor.getProcessCpuUsage() <= 100f);
        Assert.assertTrue("System CPU usage between 0 and 100", cpuMonitor.getSystemCpuUsage() >= 0f && cpuMonitor.getSystemCpuUsage() <= 100f);
        Assert.assertTrue("Available processors >= 1", cpuMonitor.getAvailableProcessors() >= 1);
    }

    @Test
    public void testMemoryWatchdogMetrics() {
        memoryWatchdog.sample();
        Assert.assertTrue("Used heap >= 0", memoryWatchdog.getUsedHeapMb() >= 0);
        Assert.assertTrue("Max heap > 0", memoryWatchdog.getMaxHeapMb() > 0);
        Assert.assertTrue("Usage percent between 0 and 100", memoryWatchdog.getHeapUsagePercent() >= 0f && memoryWatchdog.getHeapUsagePercent() <= 100f);

        int initialGc = memoryWatchdog.getGcCount();
        memoryWatchdog.notifyGcExecuted(15);
        Assert.assertEquals(initialGc + 1, memoryWatchdog.getGcCount());
        Assert.assertTrue("GC estimated pause tracked", memoryWatchdog.getGcEstimatedPauseMs() >= 15);
    }

    @Test
    public void testFrameLatencyTracker() {
        // Record 60 normal frames (16.6ms each)
        for (int i = 0; i < 60; i++) {
            frameTracker.recordFrame(16_666_666L);
        }
        Assert.assertEquals(0, frameTracker.getJankCount());
        Assert.assertEquals(0, frameTracker.getFreezeCount());
        Assert.assertTrue("FPS around 60", frameTracker.getAverageFps() > 40f);

        // Record a jank frame (40ms) and frozen frame (300ms)
        frameTracker.recordFrame(40_000_000L);
        Assert.assertEquals(1, frameTracker.getJankCount());

        frameTracker.recordFrame(300_000_000L);
        Assert.assertEquals(1, frameTracker.getFreezeCount());

        frameTracker.resetCounters();
        Assert.assertEquals(0, frameTracker.getJankCount());
        Assert.assertEquals(0, frameTracker.getFreezeCount());
    }
}
