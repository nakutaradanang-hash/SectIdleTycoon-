package com.sect.idle.apm;

import android.os.Debug;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MemoryWatchdog - Real-time JVM heap, Native heap, and GC frequency monitoring.
 */
public final class MemoryWatchdog {

    private final Runtime runtime;
    private long lastSampleTime = 0L;
    private long lastAllocatedBytes = 0L;
    
    private final AtomicInteger gcCount = new AtomicInteger(0);
    private final AtomicLong gcEstimatedPauseMs = new AtomicLong(0L);

    public MemoryWatchdog() {
        this.runtime = Runtime.getRuntime();
        this.lastAllocatedBytes = runtime.totalMemory() - runtime.freeMemory();
    }

    public void sample() {
        long currentAllocated = runtime.totalMemory() - runtime.freeMemory();
        // If current allocated drops significantly while no manual free happened, GC likely ran
        if (lastAllocatedBytes > currentAllocated + (1024 * 1024)) {
            gcCount.incrementAndGet();
            long reclaimed = lastAllocatedBytes - currentAllocated;
            long estimatedPause = Math.max(2, Math.min(50, reclaimed / (1024 * 512)));
            gcEstimatedPauseMs.addAndGet(estimatedPause);
        }
        lastAllocatedBytes = currentAllocated;
        lastSampleTime = System.currentTimeMillis();
    }

    public long getUsedHeapMb() {
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    public long getMaxHeapMb() {
        return runtime.maxMemory() / (1024 * 1024);
    }

    public float getHeapUsagePercent() {
        long max = runtime.maxMemory();
        if (max <= 0) return 0f;
        long used = runtime.totalMemory() - runtime.freeMemory();
        return (used * 100f) / max;
    }

    public long getNativeHeapAllocatedMb() {
        try {
            return Debug.getNativeHeapAllocatedSize() / (1024 * 1024);
        } catch (Throwable t) {
            return 0L;
        }
    }

    public int getGcCount() {
        return gcCount.get();
    }

    public long getGcEstimatedPauseMs() {
        return gcEstimatedPauseMs.get();
    }

    public void notifyGcExecuted(long pauseMs) {
        gcCount.incrementAndGet();
        gcEstimatedPauseMs.addAndGet(pauseMs);
    }

    public void triggerSystemGc() {
        System.gc();
        sample();
    }
}
