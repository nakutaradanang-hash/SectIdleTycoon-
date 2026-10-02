package com.sect.idle.apm;

import android.os.SystemClock;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FrameLatencyTracker - High-precision frame timing, FPS, and jank/freeze detector.
 */
public final class FrameLatencyTracker {

    private static final int BUFFER_SIZE = 120;
    private final long[] frameDurationsNs = new long[BUFFER_SIZE];
    private int bufferIndex = 0;
    private int recordedCount = 0;

    private long lastFrameTimeNs = 0L;
    private float cachedFps = 60.0f;
    private float cachedAvgFrameMs = 16.6f;
    private float cachedMaxFrameMs = 16.6f;

    private final AtomicInteger jankFrameCount = new AtomicInteger(0);
    private final AtomicInteger frozenFrameCount = new AtomicInteger(0);

    private static long safeElapsedRealtimeNanos() {
        try {
            return SystemClock.elapsedRealtimeNanos();
        } catch (Throwable t) {
            return System.nanoTime();
        }
    }

    public FrameLatencyTracker() {
        lastFrameTimeNs = safeElapsedRealtimeNanos();
    }

    public synchronized void recordFrame(long frameDurationNs) {
        if (frameDurationNs <= 0) return;
        
        frameDurationsNs[bufferIndex] = frameDurationNs;
        bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
        if (recordedCount < BUFFER_SIZE) {
            recordedCount++;
        }

        long frameMs = frameDurationNs / 1_000_000L;
        if (frameMs >= 30) {
            jankFrameCount.incrementAndGet();
        }
        if (frameMs >= 250) {
            frozenFrameCount.incrementAndGet();
        }

        if (bufferIndex % 15 == 0) {
            recalculateMetrics();
        }
    }

    public synchronized void recordFrameTime(long frameDurationNs) {
        recordFrame(frameDurationNs);
    }

    public int getJankCount() {
        return jankFrameCount.get();
    }

    public int getFreezeCount() {
        return frozenFrameCount.get();
    }

    public float getAverageFps() {
        return cachedFps;
    }

    public synchronized void onFrameRendered() {
        long now = safeElapsedRealtimeNanos();
        if (lastFrameTimeNs > 0) {
            long duration = now - lastFrameTimeNs;
            recordFrame(duration);
        }
        lastFrameTimeNs = now;
    }

    private void recalculateMetrics() {
        if (recordedCount == 0) return;
        long sumNs = 0;
        long maxNs = 0;

        for (int i = 0; i < recordedCount; i++) {
            long d = frameDurationsNs[i];
            sumNs += d;
            if (d > maxNs) maxNs = d;
        }

        float avgMs = (sumNs / (float) recordedCount) / 1_000_000f;
        cachedAvgFrameMs = avgMs;
        cachedMaxFrameMs = maxNs / 1_000_000f;
        cachedFps = avgMs > 0.001f ? Math.min(120.0f, 1000f / avgMs) : 60.0f;
    }

    public float getFps() {
        return cachedFps;
    }

    public float getAvgFrameMs() {
        return cachedAvgFrameMs;
    }

    public float getMaxFrameMs() {
        return cachedMaxFrameMs;
    }

    public int getJankFrameCount() {
        return jankFrameCount.get();
    }

    public int getFrozenFrameCount() {
        return frozenFrameCount.get();
    }

    public void resetCounters() {
        jankFrameCount.set(0);
        frozenFrameCount.set(0);
    }
}
