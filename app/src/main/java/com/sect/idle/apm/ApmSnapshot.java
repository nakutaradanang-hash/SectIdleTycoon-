package com.sect.idle.apm;

import java.util.Locale;

/**
 * ApmSnapshot - Immutable point-in-time metrics snapshot for APM telemetry.
 */
public final class ApmSnapshot {
    public final long timestamp;
    
    // CPU Metrics
    public final float processCpuPercent;
    public final float systemCpuPercent;
    
    // Memory Metrics (MB)
    public final long usedHeapMb;
    public final long maxHeapMb;
    public final float heapUsagePercent;
    public final long nativeHeapAllocatedMb;
    public final int gcCountTotal;
    public final long gcPauseEstimateMs;
    
    // Rendering & Frame Metrics
    public final float fps;
    public final float avgFrameMs;
    public final float maxFrameMs;
    public final int jankFrameCount;
    public final int frozenFrameCount;
    
    // Error & Quality Metrics
    public final int totalExceptions;
    public final int npeCount;
    public final int oomCount;
    public final int crashCount;
    public final float errorsPerMinute;
    
    // Leaks & Resources
    public final int activeLeakCandidates;
    public final int activeThreadCount;

    public ApmSnapshot(long timestamp, float processCpuPercent, float systemCpuPercent,
                       long usedHeapMb, long maxHeapMb, float heapUsagePercent,
                       long nativeHeapAllocatedMb, int gcCountTotal, long gcPauseEstimateMs,
                       float fps, float avgFrameMs, float maxFrameMs,
                       int jankFrameCount, int frozenFrameCount,
                       int totalExceptions, int npeCount, int oomCount, int crashCount,
                       float errorsPerMinute, int activeLeakCandidates, int activeThreadCount) {
        this.timestamp = timestamp;
        this.processCpuPercent = processCpuPercent;
        this.systemCpuPercent = systemCpuPercent;
        this.usedHeapMb = usedHeapMb;
        this.maxHeapMb = maxHeapMb;
        this.heapUsagePercent = heapUsagePercent;
        this.nativeHeapAllocatedMb = nativeHeapAllocatedMb;
        this.gcCountTotal = gcCountTotal;
        this.gcPauseEstimateMs = gcPauseEstimateMs;
        this.fps = fps;
        this.avgFrameMs = avgFrameMs;
        this.maxFrameMs = maxFrameMs;
        this.jankFrameCount = jankFrameCount;
        this.frozenFrameCount = frozenFrameCount;
        this.totalExceptions = totalExceptions;
        this.npeCount = npeCount;
        this.oomCount = oomCount;
        this.crashCount = crashCount;
        this.errorsPerMinute = errorsPerMinute;
        this.activeLeakCandidates = activeLeakCandidates;
        this.activeThreadCount = activeThreadCount;
    }

    public String getFormattedSummary() {
        return String.format(Locale.US,
                "CPU: %.1f%% | Heap: %dMB/%dMB (%.1f%%) | Native: %dMB | FPS: %.1f (%.1fms) | Jank: %d | Err: %d (%.1f/m) | Leaks: %d | Threads: %d",
                processCpuPercent, usedHeapMb, maxHeapMb, heapUsagePercent, nativeHeapAllocatedMb,
                fps, avgFrameMs, jankFrameCount, totalExceptions, errorsPerMinute, activeLeakCandidates, activeThreadCount);
    }
}
