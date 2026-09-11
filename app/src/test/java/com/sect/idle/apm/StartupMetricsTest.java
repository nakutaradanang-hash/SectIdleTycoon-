package com.sect.idle.apm;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StartupMetricsTest {

    private StartupMetricsTracker tracker;

    @Before
    public void setUp() {
        tracker = StartupMetricsTracker.get();
        tracker.recordAppStart();
    }

    @Test
    public void testStartupMilestonesAndVitals() {
        tracker.recordSplashCreated();
        tracker.recordSplashReady();
        tracker.recordGameActivityCreated();
        tracker.recordFirstFrameDrawn();
        tracker.recordFullAssetsLoaded();

        assertTrue(tracker.getTtidMs() >= 0);
        assertTrue(tracker.getTtfdMs() >= 0);
        assertTrue(tracker.isStartupComplete());
    }

    @Test
    public void testInputLatencyAndLayoutShift() {
        tracker.recordInputLatency(12.5f);
        tracker.recordInputLatency(15.2f);
        assertEquals(15.2f, tracker.getMaxInteractionDelayMs(), 0.001f);

        tracker.recordLayoutShift(0.005f);
        tracker.recordLayoutShift(0.005f);
        assertEquals(0.010f, tracker.getCumulativeLayoutShift(), 0.001f);
    }

    @Test
    public void testStartupJankTracking() {
        int initialJank = tracker.getStartupJankFrames();
        tracker.recordStartupJank();
        assertEquals(initialJank + 1, tracker.getStartupJankFrames());
    }

    @Test
    public void testFormattedSummaryNotNull() {
        String summary = tracker.getFormattedSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("TTID"));
        assertTrue(summary.contains("TTFD"));
    }
}
