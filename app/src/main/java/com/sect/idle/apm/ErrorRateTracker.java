package com.sect.idle.apm;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ErrorRateTracker - Tracks application error rates (NPE, OOM, FC, etc.) and error velocity.
 */
public final class ErrorRateTracker {

    private final AtomicInteger totalExceptions = new AtomicInteger(0);
    private final AtomicInteger npeCount = new AtomicInteger(0);
    private final AtomicInteger oomCount = new AtomicInteger(0);
    private final AtomicInteger crashCount = new AtomicInteger(0);

    private final LinkedList<Long> errorTimestamps = new LinkedList<Long>();
    private static final long SLIDING_WINDOW_MS = 60_000L; // 1 minute window

    public synchronized void recordException(Throwable t, boolean isFatal) {
        if (t == null) return;

        long now = System.currentTimeMillis();
        totalExceptions.incrementAndGet();
        errorTimestamps.addLast(now);

        if (isFatal) {
            crashCount.incrementAndGet();
        }

        if (t instanceof NullPointerException) {
            npeCount.incrementAndGet();
        } else if (t instanceof OutOfMemoryError) {
            oomCount.incrementAndGet();
        }

        pruneOldTimestamps(now);
    }

    private void pruneOldTimestamps(long now) {
        while (!errorTimestamps.isEmpty() && now - errorTimestamps.getFirst() > SLIDING_WINDOW_MS) {
            errorTimestamps.removeFirst();
        }
    }

    public synchronized float getErrorsPerMinute() {
        long now = System.currentTimeMillis();
        pruneOldTimestamps(now);
        return errorTimestamps.size();
    }

    public int getTotalExceptions() {
        return totalExceptions.get();
    }

    public int getNpeCount() {
        return npeCount.get();
    }

    public int getOomCount() {
        return oomCount.get();
    }

    public int getCrashCount() {
        return crashCount.get();
    }

    public synchronized void reset() {
        totalExceptions.set(0);
        npeCount.set(0);
        oomCount.set(0);
        crashCount.set(0);
        errorTimestamps.clear();
    }
}
