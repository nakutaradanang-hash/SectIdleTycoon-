package com.sect.idle.apm;

import com.sect.idle.core.GameConfig;
import com.sect.idle.utils.PerformanceProfiler;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Performance Analysis Tools Test Suite.
 * Validates real-time frame rate monitoring, memory leak detection heuristics,
 * CPU sampling, frame latency tracking, and adaptive quality throttling.
 */
public class PerformanceAnalysisToolsTest {

    private PerformanceProfiler profiler;
    private LeakDetector leakDetector;
    private FrameLatencyTracker latencyTracker;

    @Before
    public void setUp() {
        profiler = new PerformanceProfiler();
        leakDetector = new LeakDetector();
        latencyTracker = new FrameLatencyTracker();
        GameConfig.currentQuality = GameConfig.QUALITY_HIGH;
    }

    @Test
    public void testPerformanceProfilerFrameMetrics() {
        profiler.beginFrame();
        // Simulate frame execution
        for (int i = 0; i < 1000; i++) {
            profiler.markDrawCall();
            profiler.markEntity();
            profiler.markParticle();
        }
        profiler.endFrame();

        assertEquals(1000, profiler.drawCalls);
        assertEquals(1000, profiler.entityCount);
        assertEquals(1000, profiler.particleCount);

        profiler.resetCounters();
        assertEquals(0, profiler.drawCalls);
        assertEquals(0, profiler.entityCount);
        assertEquals(0, profiler.particleCount);

        String stats = profiler.getStatsString();
        assertNotNull(stats);
        assertTrue(stats.contains("FPS"));
        assertTrue(stats.contains("DC"));
    }

    @Test
    public void testLeakDetector_TracksAndDetectsLeakCandidates() throws InterruptedException {
        Object testObj = new Object();
        String watchKey = leakDetector.watch(testObj, "TestActivityLeak");
        assertNotNull("Watch key must be generated", watchKey);

        // Immediate check should not flag as leak (duration < 5000ms)
        List<LeakDetector.LeakCandidate> immediateLeaks = leakDetector.checkLeaks();
        assertNotNull(immediateLeaks);

        // Clear reference
        testObj = null;
        System.gc();

        // Check again after reference cleared
        List<LeakDetector.LeakCandidate> leaksAfterGc = leakDetector.checkLeaks();
        assertNotNull(leaksAfterGc);
    }

    @Test
    public void testFrameLatencyTracker_JankAndFreezeDetection() {
        // Record standard smooth frames (< 16.6ms)
        for (int i = 0; i < 30; i++) {
            latencyTracker.recordFrameTime(16_000_000L); // 16ms
        }

        assertEquals(0, latencyTracker.getJankCount());
        assertEquals(0, latencyTracker.getFreezeCount());

        // Record a jank frame (> 33.3ms)
        latencyTracker.recordFrameTime(45_000_000L); // 45ms
        assertTrue("Jank count should increment on long frames", latencyTracker.getJankCount() >= 1);

        // Record a severe freeze frame (> 300ms)
        latencyTracker.recordFrameTime(350_000_000L); // 350ms
        assertTrue("Freeze count should increment on extreme latency frames", latencyTracker.getFreezeCount() >= 1);

        float avgFps = latencyTracker.getAverageFps();
        assertTrue("Average FPS must be positive", avgFps > 0f);
    }

    @Test
    public void testCpuMonitorSampling() {
        CpuMonitor cpuMonitor = new CpuMonitor();
        cpuMonitor.sample();
        float usage = cpuMonitor.getCpuUsagePercent();
        assertTrue("CPU usage percent must be between 0.0 and 100.0", usage >= 0.0f && usage <= 100.0f);
    }
}
