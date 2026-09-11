package com.sect.idle.apm;

import android.os.SystemClock;
import android.util.Log;
import java.util.Locale;

/**
 * StartupMetricsTracker - Measures and reports Android app launch benchmarks,
 * cold-start bottlenecks, Time to Initial Display (TTID), Time to Full Display (TTFD),
 * and Core Web Vitals equivalent metrics (FCP, LCP, INP, CLS).
 */
public final class StartupMetricsTracker {

    private static final String TAG = "StartupMetrics";
    private static final StartupMetricsTracker INSTANCE = new StartupMetricsTracker();

    // Timestamps (uptimeMillis)
    private long appStartTimeMs = 0L;
    private long splashCreatedTimeMs = 0L;
    private long splashReadyTimeMs = 0L;
    private long gameActivityCreatedTimeMs = 0L;
    private long firstFrameDrawnTimeMs = 0L;
    private long fullAssetsLoadedTimeMs = 0L;

    // Core Web Vitals & Startup Metrics
    private long ttidMs = 0L;          // Time to Initial Display (FCP equiv)
    private long ttfdMs = 0L;          // Time to Full Display (LCP equiv)
    private float maxInteractionDelayMs = 0.0f; // INP equiv
    private float cumulativeLayoutShift = 0.0f;  // CLS equiv
    private long peakMemoryMb = 0L;
    private int startupJankFrames = 0;

    private boolean isStartupComplete = false;

    private StartupMetricsTracker() {
        this.appStartTimeMs = nowMs();
    }

    private static long nowMs() {
        try {
            return SystemClock.uptimeMillis();
        } catch (Throwable t) {
            return System.currentTimeMillis();
        }
    }

    private static void logInfo(String tag, String msg) {
        try {
            Log.i(tag, msg);
        } catch (Throwable ignored) {}
    }

    public static StartupMetricsTracker get() {
        return INSTANCE;
    }

    public synchronized void recordAppStart() {
        if (appStartTimeMs == 0L) {
            appStartTimeMs = nowMs();
        }
    }

    public synchronized void recordSplashCreated() {
        this.splashCreatedTimeMs = nowMs();
    }

    public synchronized void recordSplashReady() {
        this.splashReadyTimeMs = nowMs();
    }

    public synchronized void recordGameActivityCreated() {
        this.gameActivityCreatedTimeMs = nowMs();
    }

    public synchronized void recordFirstFrameDrawn() {
        if (firstFrameDrawnTimeMs == 0L) {
            firstFrameDrawnTimeMs = nowMs();
            long base = appStartTimeMs > 0 ? appStartTimeMs : splashCreatedTimeMs;
            this.ttidMs = firstFrameDrawnTimeMs - base;
            logInfo(TAG, "TTID (Time to Initial Display / FCP): " + ttidMs + " ms");
        }
    }

    public synchronized void recordFullAssetsLoaded() {
        if (!isStartupComplete) {
            fullAssetsLoadedTimeMs = nowMs();
            long base = appStartTimeMs > 0 ? appStartTimeMs : splashCreatedTimeMs;
            this.ttfdMs = fullAssetsLoadedTimeMs - base;
            this.isStartupComplete = true;

            long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            this.peakMemoryMb = usedMem;

            logInfo(TAG, "TTFD (Time to Full Display / LCP): " + ttfdMs + " ms | Memory: " + usedMem + " MB");
        }
    }

    public synchronized void recordInputLatency(float latencyMs) {
        if (latencyMs > maxInteractionDelayMs) {
            maxInteractionDelayMs = latencyMs;
        }
    }

    public synchronized void recordLayoutShift(float shift) {
        cumulativeLayoutShift += shift;
    }

    public synchronized void recordStartupJank() {
        startupJankFrames++;
    }

    public long getTtidMs() {
        return ttidMs > 0 ? ttidMs : (firstFrameDrawnTimeMs > 0 ? firstFrameDrawnTimeMs - appStartTimeMs : 0);
    }

    public long getTtfdMs() {
        return ttfdMs > 0 ? ttfdMs : (fullAssetsLoadedTimeMs > 0 ? fullAssetsLoadedTimeMs - appStartTimeMs : 0);
    }

    public float getMaxInteractionDelayMs() {
        return maxInteractionDelayMs;
    }

    public float getCumulativeLayoutShift() {
        return cumulativeLayoutShift;
    }

    public long getPeakMemoryMb() {
        return peakMemoryMb;
    }

    public int getStartupJankFrames() {
        return startupJankFrames;
    }

    public boolean isStartupComplete() {
        return isStartupComplete;
    }

    public String getFormattedSummary() {
        return String.format(Locale.US,
                "Startup Metrics -> TTID: %d ms | TTFD: %d ms | INP: %.1f ms | CLS: %.3f | Peak Heap: %d MB | Jank Frames: %d",
                getTtidMs(), getTtfdMs(), maxInteractionDelayMs, cumulativeLayoutShift, peakMemoryMb, startupJankFrames);
    }
}
