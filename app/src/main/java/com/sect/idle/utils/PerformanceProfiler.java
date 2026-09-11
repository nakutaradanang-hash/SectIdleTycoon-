package com.sect.idle.utils;

import android.os.SystemClock;
import com.sect.idle.core.GameConfig;

/**
 * PerformanceProfiler v2.1 - Real-time FPS, frame timing, memory usage and auto quality throttle.
 */
public final class PerformanceProfiler {
    private static final int HISTORY_SIZE = 60;
    private static final float TARGET_FRAME_MS = 1000f / 30f;

    private final long[] frameTimes;
    private final long[] frameStartTimes;
    private int frameIndex;
    private long lastAdjustTime;
    private int lowFpsCount;
    private int highFpsCount;

    public float avgFps;
    public float avgFrameMs;
    public float minFrameMs;
    public float maxFrameMs;
    public int drawCalls;
    public int entityCount;
    public int particleCount;
    public int textureSwitches;
    public long usedMemoryMB;
    public long totalMemoryMB;
    public boolean throttling;

    private final Runtime runtime;
    private final StringBuilder sb;

    public PerformanceProfiler() {
        frameTimes = new long[HISTORY_SIZE];
        frameStartTimes = new long[HISTORY_SIZE];
        frameIndex = 0;
        runtime = Runtime.getRuntime();
        sb = new StringBuilder(128);
        lastAdjustTime = SystemClock.elapsedRealtime();
        throttling = false;
    }

    public void beginFrame() {
        frameStartTimes[frameIndex] = SystemClock.elapsedRealtimeNanos();
    }

    public void endFrame() {
        long now = SystemClock.elapsedRealtimeNanos();
        long elapsed = now - frameStartTimes[frameIndex];
        if (elapsed < 0) elapsed = 0;
        frameTimes[frameIndex] = elapsed;
        frameIndex = (frameIndex + 1) % HISTORY_SIZE;

        if (frameIndex % 30 == 0) {
            updateStats();
            updateMemory();
            autoAdjustQuality();
        }
    }

    private void updateStats() {
        long sum = 0, min = Long.MAX_VALUE, max = 0;
        int count = 0;
        for (long t : frameTimes) {
            if (t > 0) {
                sum += t;
                if (t < min) min = t;
                if (t > max) max = t;
                count++;
            }
        }
        if (count > 0) {
            avgFrameMs = sum / (count * 1_000_000f);
            minFrameMs = min / 1_000_000f;
            maxFrameMs = max / 1_000_000f;
            avgFps = 1000f / Math.max(avgFrameMs, 0.001f);
        }
    }

    private void updateMemory() {
        usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        totalMemoryMB = runtime.maxMemory() / 1024 / 1024;
    }

    private void autoAdjustQuality() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastAdjustTime < 10000) return;
        lastAdjustTime = now;

        int current = GameConfig.currentQuality;

        if (avgFrameMs > TARGET_FRAME_MS * 1.5f || usedMemoryMB > totalMemoryMB * 0.85f) {
            lowFpsCount++;
            highFpsCount = 0;
            if (lowFpsCount >= 3 && current > GameConfig.QUALITY_LOW) {
                GameConfig.setQuality(current - 1);
                lowFpsCount = 0;
                throttling = true;
            }
        } else if (avgFrameMs < TARGET_FRAME_MS * 0.7f && usedMemoryMB < totalMemoryMB * 0.6f) {
            highFpsCount++;
            lowFpsCount = 0;
            if (highFpsCount >= 5 && current < GameConfig.QUALITY_ULTRA) {
                GameConfig.setQuality(current + 1);
                highFpsCount = 0;
                throttling = false;
            }
        } else {
            lowFpsCount = Math.max(0, lowFpsCount - 1);
            highFpsCount = Math.max(0, highFpsCount - 1);
        }
    }

    public String getStatsString() {
        sb.setLength(0);
        sb.append("FPS: ").append(String.format(java.util.Locale.US, "%.1f", avgFps));
        sb.append(" | MS: ").append(String.format(java.util.Locale.US, "%.1f", avgFrameMs));
        sb.append(" | DC: ").append(drawCalls);
        sb.append(" | MEM: ").append(usedMemoryMB).append("/").append(totalMemoryMB).append("MB");
        if (throttling) sb.append(" [THROTTLE]");
        return sb.toString();
    }

    public void resetCounters() {
        drawCalls = 0;
        entityCount = 0;
        particleCount = 0;
        textureSwitches = 0;
    }

    public void markDrawCall() { drawCalls++; }
    public void markEntity() { entityCount++; }
    public void markParticle() { particleCount++; }
    public void markTextureSwitch() { textureSwitches++; }
}
